document.addEventListener('DOMContentLoaded', function () {

    document.body.addEventListener('htmx:beforeOnLoad', function (evt) {
        const status = evt.detail.xhr.status;

        if (status >= 400) {
            evt.detail.shouldSwap = true;
            evt.detail.isError = false;
        }
    });
});

function checkPasswordMatch(input) {
    const password = document.getElementsByName('newPassword')[0].value;
    if (input.value !== password) {
        input.setCustomValidity('Passwords do not match');
    } else {
        input.setCustomValidity('');
    }
}

function copyToClipBoard(btnElement) {
    const idToCopy = btnElement.getAttribute("data-id");

    navigator.clipboard.writeText(idToCopy).then(() => {

        const originalHtml = btnElement.innerHTML;
        const originalClass = 'btn-outline-secondary';
        const successClass = 'btn-success';

        btnElement.innerHTML = '<i class="fas fa-check"></i>';
        btnElement.classList.replace(originalClass, successClass);

        setTimeout(() => {
            btnElement.innerHTML = originalHtml;
            btnElement.classList.replace(successClass, originalClass);
        }, 2000);
    }).catch(err => {
        console.error('Failed to copy: ', err);
    });
}