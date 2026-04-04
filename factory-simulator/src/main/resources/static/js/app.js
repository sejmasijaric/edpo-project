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
  const simulateVgrFailure = document.getElementById('simulate-vgr-failure');
  let loadStateInProgress = false;
  const selectedTargets = new Map();

  if (!itemList || !status || !addItemForm || !addItemSink || !factoryCanvas || !simulateVgrFailure) {
    return;
  }

  function isEditingTargetSink() {
    return document.activeElement?.matches('[data-role="target-sink"]') ?? false;
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

      const [itemsResponse, sinksResponse, vgrStatusResponse, wtStatusResponse, smStatusResponse, ovStatusResponse, vgrFailureResponse] = await Promise.all([
        fetch('/api/items'),
        fetch('/api/sinks'),
        fetch('/api/vgr/status'),
        fetch('/api/wt/status'),
        fetch('/api/sm/status'),
        fetch('/api/ov/status'),
        fetch('/api/vgr/failure-simulation')
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
      if (!wtStatusResponse.ok) {
        throw new Error(await parseError(wtStatusResponse));
      }
      if (!smStatusResponse.ok) {
        throw new Error(await parseError(smStatusResponse));
      }
      if (!ovStatusResponse.ok) {
        throw new Error(await parseError(ovStatusResponse));
      }
      if (!vgrFailureResponse.ok) {
        throw new Error(await parseError(vgrFailureResponse));
      }


      const items = await itemsResponse.json();
      const sinks = await sinksResponse.json();
      const vgrStatus = await vgrStatusResponse.json();
      const wtStatus = await wtStatusResponse.json();
      const smStatus = await smStatusResponse.json();
      const ovStatus = await ovStatusResponse.json();
      const vgrFailureSimulation = await vgrFailureResponse.json();

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

      const currentItemIds = new Set(items.map((item) => item.id));
      Array.from(selectedTargets.keys()).forEach((itemId) => {
        if (!currentItemIds.has(itemId)) {
          selectedTargets.delete(itemId);
        }
      });

      if (!isEditingTargetSink()) {
        itemList.innerHTML = items.length === 0
            ? '<p class="empty-state">No items are currently in the simulator.</p>'
            : items.map((item) => renderItemCard(item, sinks, selectedTargets.get(item.id))).join('');
      }

      syncSinkPositions(sinks);
      syncMachineIndicator(vgrStatus);
      syncMachineIndicator(wtStatus);
      syncMachineIndicator(smStatus);
      syncMachineIndicator(ovStatus);
      simulateVgrFailure.checked = Boolean(vgrFailureSimulation.enabled);

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
        selectedTargets.delete(itemId);
      } else {
        const targetSink = itemCard.querySelector('[data-role="target-sink"]').value;
        const response = await fetch(`/api/items/${itemId}/move?targetSinkId=${encodeURIComponent(targetSink)}`, {
          method: 'POST'
        });
        if (!response.ok) {
          throw new Error(await parseError(response));
        }
        selectedTargets.set(itemId, targetSink);
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

  itemList.addEventListener('change', (event) => {
    const targetSinkSelector = event.target.closest('[data-role="target-sink"]');
    if (!targetSinkSelector) {
      return;
    }

    const itemCard = targetSinkSelector.closest('[data-item-id]');
    const itemId = itemCard?.dataset.itemId;
    if (!itemId) {
      return;
    }

    selectedTargets.set(itemId, targetSinkSelector.value);
  });

  itemList.addEventListener('focusout', (event) => {
    if (!event.target.closest('[data-role="target-sink"]')) {
      return;
    }

    window.setTimeout(() => {
      if (!isEditingTargetSink()) {
        loadState({silent: true}).catch((error) => {
          status.textContent = error.message;
        });
      }
    }, 0);
  });

  simulateVgrFailure.addEventListener('change', async () => {
    const enabled = simulateVgrFailure.checked;

    try {
      simulateVgrFailure.disabled = true;
      status.textContent = `${enabled ? 'Enabling' : 'Disabling'} vacuum gripper failure simulation…`;

      const response = await fetch(`/api/vgr/failure-simulation?enabled=${encodeURIComponent(enabled)}`, {
        method: 'POST'
      });
      if (!response.ok) {
        throw new Error(await parseError(response));
      }

      await loadState({
        message: 'Refreshing simulator state…',
        successMessage: `Vacuum gripper failure simulation ${enabled ? 'enabled' : 'disabled'}.`
      });
    } catch (error) {
      simulateVgrFailure.checked = !enabled;
      status.textContent = error.message;
    } finally {
      simulateVgrFailure.disabled = false;
    }
  });

  window.setInterval(() => {
    loadState({silent: true}).catch((error) => {
      status.textContent = error.message;
    });
  }, LIVE_UPDATE_INTERVAL_MS);
});
