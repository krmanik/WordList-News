function turnOnDevTools() {
    eruda.init();

    eruda._entryBtn.hide();
    eruda._devTools._$el[0].style.height = "80%";

    if (eruda.get()._isShow) {
        eruda.hide();
    } else {
        eruda.show();
    }
}
turnOnDevTools();