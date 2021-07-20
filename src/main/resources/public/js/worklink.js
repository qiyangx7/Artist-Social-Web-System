var lists = document.getElementsByClassName("content work");
for(var i=0; i<lists.length; i++) {
    var text = lists[i].textContent;
    var split = text.split(" split ")
    lists[i].textContent = "";
    var a = document.createElement("a");
    a.href = "/works/"+ split[1];
    a.textContent = split[0];
    lists[i].appendChild(a);
}