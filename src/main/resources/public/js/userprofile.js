var favorites = document.getElementById("favorites");
var favArray = favorites.textContent.split(",");
console.log(favArray)
favorites.textContent = "";
for (var i = 0; i < favArray.length; i++) {
    favorites.textContent = favorites.textContent + " "
        + favArray[i] + " ";
}
