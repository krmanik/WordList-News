function getMeaning() {

     var t = document.querySelectorAll("h4[data-cy='translation']");
     var words = [];

     for (i=0; i < t.length;i++) {
         words.push(t[i].innerText);
     }

    console.log(words);

    JSInterface.getWordData(JSON.stringify(words));
}