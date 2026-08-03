(() => {
    const urlInput = document.getElementById('imageUrl');
    const fileInput = document.getElementById('imageFile');
    const preview = document.getElementById('imagePreview');
    if (!preview) return;

    const placeholder = '/images/placeholder.svg';
    urlInput?.addEventListener('input', () => {
        if (!fileInput?.files?.length) preview.src = urlInput.value.trim() || placeholder;
    });
    fileInput?.addEventListener('change', () => {
        const file = fileInput.files?.[0];
        if (file) preview.src = URL.createObjectURL(file);
        else preview.src = urlInput?.value.trim() || placeholder;
    });
})();
