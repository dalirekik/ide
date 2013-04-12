﻿/*
 Copyright (c) 2003-2010, CKSource - Frederico Knabben. All rights reserved.
 For licensing, see LICENSE.html or http://ckeditor.com/license
 */

CKEDITOR.dialog.add('uicolor', function (a) {
    var b, c, d, e = a.getUiColor();

    function f(i) {
        if (/^#/.test(i))i = window.YAHOO.util.Color.hex2rgb(i.substr(1));
        c.setValue(i, true);
        c.refresh('cke_uicolor_picker');
    };
    function g(i, j) {
        if (j || b._.contents.tab1.livePeview.getValue())a.setUiColor(i);
        b._.contents.tab1.configBox.setValue('config.uiColor = "#' + c.get('hex') + '"');
    };
    d = {id: 'yuiColorPicker', type: 'html', html: "<div id='cke_uicolor_picker' style='width: 360px; height: 200px; position: relative;'></div>", onLoad: function (i) {
        var j = CKEDITOR.getUrl('plugins/uicolor/yui/');
        c = new window.YAHOO.widget.ColorPicker('cke_uicolor_picker', {showhsvcontrols: true, showhexcontrols: true, images: {PICKER_THUMB: j + 'assets/picker_thumb.png', HUE_THUMB: j + 'assets/hue_thumb.png'}});
        if (e)f(e);
        c.on('rgbChange', function () {
            b._.contents.tab1.predefined.setValue('');
            g('#' + c.get('hex'));
        });
        var k = new CKEDITOR.dom.nodeList(c.getElementsByTagName('input'));
        for (var l = 0; l < k.count(); l++)k.getItem(l).addClass('cke_dialog_ui_input_text');
    }};
    var h = true;
    return{title: a.lang.uicolor.title, minWidth: 360, minHeight: 320, onLoad: function () {
        b = this;
        this.setupContent();
        if (CKEDITOR.env.ie7Compat)b.parts.contents.setStyle('overflow', 'hidden');
    }, contents: [
        {id: 'tab1', label: '', title: '', expand: true, padding: 0, elements: [d, {id: 'tab1', type: 'vbox', children: [
            {id: 'livePeview', type: 'checkbox', label: a.lang.uicolor.preview, 'default': 1, onLoad: function () {
                h = true;
            }, onChange: function () {
                if (h)return;
                var i = this.getValue(), j = i ? '#' + c.get('hex') : e;
                g(j, true);
            }},
            {type: 'hbox', children: [
                {id: 'predefined', type: 'select', 'default': '', label: a.lang.uicolor.predefined, items: [
                    [''],
                    ['Light blue', '#9AB8F3'],
                    ['Sand', '#D2B48C'],
                    ['Metallic', '#949AAA'],
                    ['Purple', '#C2A3C7'],
                    ['Olive', '#A2C980'],
                    ['Happy green', '#9BD446'],
                    ['Jezebel Blue', '#14B8C4'],
                    ['Burn', '#FF893A'],
                    ['Easy red', '#FF6969'],
                    ['Pisces 3', '#48B4F2'],
                    ['Aquarius 5', '#487ED4'],
                    ['Absinthe', '#A8CF76'],
                    ['Scrambled Egg', '#C7A622'],
                    ['Hello monday', '#8E8D80'],
                    ['Lovely sunshine', '#F1E8B1'],
                    ['Recycled air', '#B3C593'],
                    ['Down', '#BCBCA4'],
                    ['Mark Twain', '#CFE91D'],
                    ['Specks of dust', '#D1B596'],
                    ['Lollipop', '#F6CE23']
                ], onChange: function () {
                    var i = this.getValue();
                    if (i) {
                        f(i);
                        g(i);
                        CKEDITOR.document.getById('predefinedPreview').setStyle('background', i);
                    } else CKEDITOR.document.getById('predefinedPreview').setStyle('background', '');
                }, onShow: function () {
                    var i = a.getUiColor();
                    if (i)this.setValue(i);
                }},
                {id: 'predefinedPreview', type: 'html', html: '<div id="cke_uicolor_preview" style="border: 1px solid black; padding: 3px; width: 30px;"><div id="predefinedPreview" style="width: 30px; height: 30px;">&nbsp;</div></div>'}
            ]},
            {id: 'configBox', type: 'text', label: a.lang.uicolor.config, onShow: function () {
                var i = a.getUiColor();
                if (i)this.setValue('config.uiColor = "' + i + '"');
            }}
        ]}]}
    ], buttons: [CKEDITOR.dialog.okButton]};
});
