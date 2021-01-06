document.body.addEventListener( 'click', function ( event ) {
    var t = '';
    if (window.getSelection && (sel = window.getSelection()).modify) {
        var s = window.getSelection();
        if (s.isCollapsed) {
            s.modify('move', 'forward', 'character');
            s.modify('move', 'backward', 'word');
            s.modify('extend', 'forward', 'word');
            t = s.toString();
            s.modify('move', 'forward', 'character');
        }
        else {
            t = s.toString();
        }
    } else if ((sel = document.selection) && sel.type != "Control") {
        var textRange = sel.createRange();
        if (!textRange.text) {
            textRange.expand("word");
        }
        while (/\s$/.test(textRange.text)) {
            textRange.moveEnd("character", -1);
        }
        t = textRange.text;
    }
    JSInterface.viewMeaningPopup(t);
} );