// Shows a classification's sub classification tick boxes only while it is ticked,
// and clears them when it is unticked.
document.querySelectorAll('[data-classification-checkbox]').forEach(box => {
  const subs = box.closest('.category-picker__group').querySelector('.category-picker__subs');
  if (!subs) return;

  function update() {
    subs.hidden = !box.checked;
    if (!box.checked) subs.querySelectorAll('input[type="checkbox"]').forEach(sub => sub.checked = false);
  }

  box.addEventListener('change', update);
  update();
});
