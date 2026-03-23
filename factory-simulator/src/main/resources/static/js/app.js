async function parseError(response) {
  const message = await response.text();
  return message || `Request failed with status ${response.status}`;
}

const LIVE_UPDATE_INTERVAL_MS = 1000;

function renderSinkItem(item) {
  if (!item) {
    return '<div class="item item-empty">Empty</div>';
  }

  return `
    <div class="item">
      <span class="item-id">${item.id}</span>
      <span class="item-color">
        <span class="color-chip" style="background:${item.color}"></span>
        <span>${item.color}</span>
      </span>
    </div>
  `;
}

function renderSinkCard(sink) {
  return `
    <header>${sink.id}</header>
    <div class="item-container">${renderSinkItem(sink.item)}</div>
  `;
}

function renderMachineIndicator(status) {
  return `
    <header>${status.machine}</header>
    <p class="machine-phase">${status.phase}</p>
  `;
}

function renderItemCard(item, sinks, selectedSinkId) {
  const options = sinks
      .map((sink) => {
        const selected = sink.id === (selectedSinkId ?? item.sinkId) ? ' selected' : '';
        return `<option value="${sink.id}"${selected}>${sink.id}</option>`;
      })
      .join('');

  return `
    <article class="item-card" data-item-id="${item.id}">
      <div class="item-card-header">
        <div>
          <h3>${item.id}</h3>
          <p>Current sink: ${item.sinkId}</p>
        </div>
        <span class="item-color">
          <span class="color-chip" style="background:${item.color}"></span>
          <span>${item.color}</span>
        </span>
      </div>
      <div class="item-card-actions">
        <button type="button" class="danger-button" data-action="delete">Delete</button>
        <label class="move-control">
          <span>Move to</span>
          <select data-role="target-sink">${options}</select>
        </label>
        <button type="button" data-action="move">Move</button>
      </div>
    </article>
  `;
}

document.addEventListener('DOMContentLoaded', () => {
  const itemList = document.getElementById('item-list');
  const status = document.getElementById('controls-status');
  const addItemForm = document.getElementById('add-item-form');
  const addItemSink = document.getElementById('add-item-sink');
  const factoryCanvas = document.querySelector('.factory-canvas');
  let loadStateInProgress = false;

  if (!itemList || !status || !addItemForm || !addItemSink || !factoryCanvas) {
    return;
  }

  function collectSelectedTargets() {
    return new Map(
        Array.from(itemList.querySelectorAll('[data-item-id]')).map((itemCard) => [
          itemCard.dataset.itemId,
          itemCard.querySelector('[data-role="target-sink"]')?.value
        ])
    );
  }

  function syncSinkPositions(sinks) {
    const sinkElements = new Map(
        Array.from(factoryCanvas.querySelectorAll('.sink')).map((sinkElement) => [
          sinkElement.dataset.sinkId,
          sinkElement
        ])
    );

    sinks.forEach((sink) => {
      let sinkElement = sinkElements.get(sink.id);
      if (!sinkElement) {
        sinkElement = document.createElement('article');
        sinkElement.className = 'sink';
        sinkElement.dataset.sinkId = sink.id;
        factoryCanvas.appendChild(sinkElement);
      }

      sinkElement.style.left = `${sink.x}px`;
      sinkElement.style.top = `${sink.y}px`;
      sinkElement.innerHTML = renderSinkCard(sink);
      sinkElements.delete(sink.id);
    });

    sinkElements.forEach((sinkElement) => sinkElement.remove());
  }

  function syncMachineIndicator(machineStatus) {
    let machineElement = factoryCanvas.querySelector(`[data-machine-id="${machineStatus.machine}"]`);
    if (!machineElement) {
      machineElement = document.createElement('article');
      machineElement.className = 'machine-indicator';
      machineElement.dataset.machineId = machineStatus.machine;
      factoryCanvas.appendChild(machineElement);
    }

    machineElement.style.left = `${machineStatus.x}px`;
    machineElement.style.top = `${machineStatus.y}px`;
    machineElement.dataset.positioned = 'true';
    machineElement.innerHTML = renderMachineIndicator(machineStatus);
    machineElement.classList.toggle('is-moving', machineStatus.performingAction);
  }

  async function loadState({message, successMessage, silent = false} = {}) {
    if (loadStateInProgress) {
      return;
    }
    loadStateInProgress = true;

    if (message) {
      status.textContent = message;
    }

    try {
      const previousAddItemSinkValue = addItemSink.value;
      const selectedTargets = collectSelectedTargets();

      const [itemsResponse, sinksResponse, vgrStatusResponse] = await Promise.all([
        fetch('/api/items'),
        fetch('/api/sinks'),
        fetch('/api/vgr/status')
      ]);

      if (!itemsResponse.ok) {
        throw new Error(await parseError(itemsResponse));
      }
      if (!sinksResponse.ok) {
        throw new Error(await parseError(sinksResponse));
      }
      if (!vgrStatusResponse.ok) {
        throw new Error(await parseError(vgrStatusResponse));
      }

      const items = await itemsResponse.json();
      const sinks = await sinksResponse.json();
      const vgrStatus = await vgrStatusResponse.json();

      addItemSink.innerHTML = sinks
          .map((sink) => {
            const disabled = sink.item ? ' disabled' : '';
            return `<option value="${sink.id}"${disabled}>${sink.id}${sink.item ? ' (occupied)' : ''}</option>`;
          })
          .join('');

      const firstAvailableSink = sinks.find((sink) => !sink.item);
      const selectedAddSinkStillAvailable = sinks.some((sink) =>
        sink.id === previousAddItemSinkValue && !sink.item
      );
      addItemSink.value = selectedAddSinkStillAvailable
          ? previousAddItemSinkValue
          : (firstAvailableSink ? firstAvailableSink.id : '');
      addItemForm.querySelector('button[type="submit"]').disabled = !firstAvailableSink;

      itemList.innerHTML = items.length === 0
          ? '<p class="empty-state">No items are currently in the simulator.</p>'
          : items.map((item) => renderItemCard(item, sinks, selectedTargets.get(item.id))).join('');

      syncSinkPositions(sinks);
      syncMachineIndicator(vgrStatus);

      if (!silent) {
        status.textContent = successMessage
            ?? `Loaded ${items.length} item${items.length === 1 ? '' : 's'}. Live updates enabled.`;
      }
    } finally {
      loadStateInProgress = false;
    }
  }

  addItemForm.addEventListener('submit', async (event) => {
    event.preventDefault();

    const submitButton = addItemForm.querySelector('button[type="submit"]');
    const formData = new FormData(addItemForm);

    try {
      submitButton.disabled = true;
      status.textContent = `Adding ${formData.get('itemId')}…`;

      const response = await fetch(`/api/items?${new URLSearchParams({
        itemId: formData.get('itemId'),
        color: formData.get('color'),
        sinkId: formData.get('sinkId')
      })}`, {
        method: 'POST'
      });
      if (!response.ok) {
        throw new Error(await parseError(response));
      }

      addItemForm.reset();
      await loadState({
        message: 'Refreshing simulator state…',
        successMessage: 'Item added. Live view updated.'
      });
    } catch (error) {
      status.textContent = error.message;
    } finally {
      submitButton.disabled = false;
    }
  });

  itemList.addEventListener('click', async (event) => {
    const button = event.target.closest('button[data-action]');
    if (!button) {
      return;
    }

    const itemCard = button.closest('[data-item-id]');
    const itemId = itemCard?.dataset.itemId;
    if (!itemId) {
      return;
    }

    try {
      button.disabled = true;
      status.textContent = `${button.dataset.action === 'delete' ? 'Deleting' : 'Moving'} ${itemId}…`;

      if (button.dataset.action === 'delete') {
        const response = await fetch(`/api/items/${itemId}`, {method: 'DELETE'});
        if (!response.ok) {
          throw new Error(await parseError(response));
        }
      } else {
        const targetSink = itemCard.querySelector('[data-role="target-sink"]').value;
        const response = await fetch(`/api/items/${itemId}/move?targetSinkId=${encodeURIComponent(targetSink)}`, {
          method: 'POST'
        });
        if (!response.ok) {
          throw new Error(await parseError(response));
        }
      }

      await loadState({
        message: 'Refreshing simulator state…',
        successMessage: `${button.dataset.action === 'delete' ? 'Item deleted' : 'Item moved'}. Live view updated.`
      });
    } catch (error) {
      status.textContent = error.message;
    } finally {
      button.disabled = false;
    }
  });

  loadState({message: 'Loading current simulator state…'}).catch((error) => {
    status.textContent = error.message;
  });

  window.setInterval(() => {
    loadState({silent: true}).catch((error) => {
      status.textContent = error.message;
    });
  }, LIVE_UPDATE_INTERVAL_MS);
});
