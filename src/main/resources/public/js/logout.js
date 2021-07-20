function logout() {

    const hostname = window.location.href;
    var host = new URL(hostname).host;

    return fetch('https://' + host + '/logout', {
            method:'POST',
        }).then(res => window.location.assign("/"));
}