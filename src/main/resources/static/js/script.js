document.addEventListener('DOMContentLoaded', function () {

    document.body.addEventListener('htmx:beforeOnLoad', function (evt) {
        if (evt.detail.xhr.status === 422) {
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