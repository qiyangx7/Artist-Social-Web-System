function addUser() {

    alert("User adding...")

    const username = document.getElementById("username").value;

    const hostname = window.location.href;
    var host = new URL(hostname).host;

    return fetch('https://' + host + '/adduser?'
        + 'username=' + username, {
            method: 'POST',
        }
    ).then(res => window.location.reload(true));

}