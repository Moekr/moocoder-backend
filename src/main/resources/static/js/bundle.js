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

function deleteStudent(id, username, email) {
    $('#delete-id-input').val(id);
    $('#delete-id').text(id);
    $('#delete-username').text(username);
    $('#delete-email').text(email);
    $('#delete-modal').modal();
}

function deprecate(id, name, language) {
    $('#deprecate-id-input').val(id);
    $('#deprecate-id').text(id);
    $('#deprecate-name').text(name);
    $('#deprecate-language').text(language);
    $('#deprecate-modal').modal();
}

function deleteExamination(id, name) {
    $('#delete-id-input').val(id);
    $('#delete-id').text(id);
    $('#delete-name').text(name);
    $('#delete-modal').modal();
}