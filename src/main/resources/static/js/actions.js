// Saves action tick boxes and deletes actions in the background, so the page doesn't reload.
// Without JavaScript the forms still submit normally.
(function () {
	function send(form) {
		return fetch(form.action, {
			method: 'POST',
			body: new FormData(form), // includes the CSRF token Thymeleaf adds to the form
			headers: { 'X-Requested-With': 'fetch' }
		}).then(function (response) {
			if (!response.ok) throw new Error('Request failed: ' + response.status);
		});
	}

	// Same order as Action.DISPLAY_ORDER: not completed first, then soonest due date (none last), then id
	function sortRows(tbody) {
		var rows = Array.from(tbody.querySelectorAll('tr[data-action-id]'));
		function key(row) {
			var box = row.querySelector('form[data-action-complete] input[type="checkbox"]');
			return { done: box && box.checked ? 1 : 0, due: row.dataset.due || '', id: Number(row.dataset.actionId) };
		}
		rows.sort(function (a, b) {
			var ka = key(a), kb = key(b);
			if (ka.done !== kb.done) return ka.done - kb.done;
			if (ka.due !== kb.due) {
				if (!ka.due) return 1;
				if (!kb.due) return -1;
				return ka.due < kb.due ? -1 : 1;
			}
			return ka.id - kb.id;
		});
		rows.forEach(function (row) { tbody.appendChild(row); });
	}

	document.querySelectorAll('form[data-action-complete] input[type="checkbox"]').forEach(function (checkbox) {
		checkbox.removeAttribute('onchange');
		checkbox.addEventListener('change', function () {
			// Send before disabling: disabled inputs are left out of the form data,
			// which would always save the action as not completed
			var request = send(checkbox.form);
			checkbox.disabled = true;
			request
				.then(function () {
					var tbody = checkbox.closest('tbody');
					if (tbody) sortRows(tbody);
					checkbox.focus();
				})
				.catch(function () {
					checkbox.checked = !checkbox.checked;
					alert('Sorry, that action could not be updated. Please try again.');
				})
				.finally(function () {
					checkbox.disabled = false;
				});
		});
	});

	document.querySelectorAll('form[data-action-delete]').forEach(function (form) {
		form.addEventListener('submit', function (e) {
			// The inline confirm() runs first and cancels the submit if the user says no
			if (e.defaultPrevented) return;
			e.preventDefault();
			send(form)
				.then(function () {
					form.closest('tr').remove();
				})
				.catch(function () {
					alert('Sorry, that action could not be deleted. Please try again.');
				});
		});
	});
})();
