function addWork(quill) {

    alert("Work adding...")

    const title = document.getElementById("title").value;
    const status = document.getElementById("status").value;
    const description = document.getElementById("description").value;
    var content = JSON.stringify(quill.getContents());

    const hostname = window.location.href;
    var host = new URL(hostname).host;

    return fetch('https://' + host + '/addwork?'
        + 'title=' + title
        + '&status=' + status
        + '&description=' + description
        + '&content=' + content, {
        method: 'POST',
        }
    ).then(res => window.location.assign("/"));
}