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

function copyTraceId() {
    const copyText = document.getElementById("traceIdInput");
    copyText.select();
    copyText.setSelectionRange(0, 99999);
    navigator.clipboard.writeText(copyText.value);

    const btn = document.querySelector('button[onclick="copyTraceId()"]');
    const originalHtml = btn.innerHTML;
    btn.innerHTML = '<i class="fas fa-check"></i> Copied';
    btn.classList.replace('btn-outline-secondary', 'btn-success');

    setTimeout(() => {
        btn.innerHTML = originalHtml;
        btn.classList.replace('btn-success', 'btn-outline-secondary');
    }, 2000);
}