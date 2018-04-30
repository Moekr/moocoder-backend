function checkInput() {
    return checkInputElement('username-input')
        && checkInputElement('email-input')
        && checkInputElement('password-input')
        && checkInputElement('confirm-input');
}

function checkInputElement(elementId) {
    var element = document.getElementById(elementId);
    return element == null || (element.value || '') !== '';
}