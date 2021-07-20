function updateWork(quill) {

    alert("Work updating...")

    const id = document.getElementById("workId").value;
    const title = document.getElementById("title").value;
    const status = document.getElementById("status").value;
    const description = document.getElementById("description").value;
    var content = JSON.stringify(quill.getContents());

    const hostname = window.location.href;
    var host = new URL(hostname).host;

    return fetch('https://' + host + '/updatework?'
        + 'id=' + id
        + '&title=' + title
        + '&status=' + status
        + '&description=' + description
        + '&content=' + content, {
        method: 'POST',
        }
    ).then(res => window.location.assign("/works/"+id));
}