async function parseError(response) {
  const message = await response.text();
  return message || `Request failed with status ${response.status}`;
}

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

function renderItemCard(item, sinks) {
  const options = sinks
      .map((sink) => {
        const selected = sink.id === item.sinkId ? ' selected' : '';
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

  if (!itemList || !status || !addItemForm || !addItemSink) {
    return;
  }

  async function loadState(message) {
    if (message) {
      status.textContent = message;
    }

    const [itemsResponse, sinksResponse] = await Promise.all([
      fetch('/api/items'),
      fetch('/api/sinks')
    ]);

    if (!itemsResponse.ok) {
      throw new Error(await parseError(itemsResponse));
    }
    if (!sinksResponse.ok) {
      throw new Error(await parseError(sinksResponse));
    }

    const items = await itemsResponse.json();
    const sinks = await sinksResponse.json();
    addItemSink.innerHTML = sinks
        .map((sink) => {
          const disabled = sink.item ? ' disabled' : '';
          return `<option value="${sink.id}"${disabled}>${sink.id}${sink.item ? ' (occupied)' : ''}</option>`;
        })
        .join('');

    const firstAvailableSink = sinks.find((sink) => !sink.item);
    addItemSink.value = firstAvailableSink ? firstAvailableSink.id : '';
    addItemForm.querySelector('button[type="submit"]').disabled = !firstAvailableSink;

    itemList.innerHTML = items.length === 0
        ? '<p class="empty-state">No items are currently in the simulator.</p>'
        : items.map((item) => renderItemCard(item, sinks)).join('');

    sinks.forEach((sink) => {
      const sinkElement = document.querySelector(`[data-sink-id="${sink.id}"] .item-container`);
      if (sinkElement) {
        sinkElement.innerHTML = renderSinkItem(sink.item);
      }
    });

    status.textContent = `Loaded ${items.length} item${items.length === 1 ? '' : 's'}.`;
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
      await loadState('Refreshing simulator state…');
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

      await loadState(`Refreshing simulator state…`);
    } catch (error) {
      status.textContent = error.message;
    } finally {
      button.disabled = false;
    }
  });

  loadState('Loading current simulator state…').catch((error) => {
    status.textContent = error.message;
  });
});
