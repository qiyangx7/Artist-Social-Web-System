function addFav(input) {
    var inputArr = input.split(",");

    const hostname = window.location.href;
    var host = new URL(hostname).host;

    fetch('https://' + host + '/addFav?username=' + inputArr[1] + "&workId="
        + inputArr[0], {
            method: 'POST',
        }
    ).then(res => window.location.reload(true));
}

