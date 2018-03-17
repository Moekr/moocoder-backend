function checkInput() {
    return checkInputElement('username-input')
        && checkInputElement('email-input')
        && checkInputElement('origin-input')
        && checkInputElement('password-input')
        && checkInputElement('confirm-input')
        && checkInputElement('name-input')
        && checkInputElement('start-at-input')
        && checkInputElement('end-at-input');
}

function checkInputElement(elementId) {
    var element = document.getElementById(elementId);
    return element == null || (element.value || '') !== '';
}

function change() {
    $('#change-modal').modal();
}

function participate(id, name) {
    $('#participate-id-input').val(id);
    $('#participate-id').text(id);
    $('#participate-name').text(name);
    $('#participate-modal').modal();
}