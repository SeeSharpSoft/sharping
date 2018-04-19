(function ($) {

    var batch = function (aData, sBoundary) {
        var aBody = [];

        aData.forEach(function(oData) {
            var sType = oData.type.toUpperCase();

            aBody.push('--' + sBoundary);
            aBody.push('Content-Type: application/http');
            aBody.push('');
            aBody.push(sType + ' ' + oData.url + ' HTTP/1.1');
            aBody.push('Host: ' + location.host);

            /* GET and DELTE do not have a body */
            if (sType !== 'GET' && sType !== 'DELETE') {
                aBody.push('Content-Type: ' + (oData.contentType || 'application/json; charset=utf-8'));
            }

            aBody.push('');
            aBody.push(oData.data ? JSON.stringify(oData.data) : '');
        });

        aBody.push('--' + sBoundary + '--');

        return aBody.join('\r\n');
    };

    var unbatch = function (sData) {
        sData = sData.replace("\r", "");
        var sBoundary = sData.substring(0, sData.indexOf("\n")),
            aBatches = sData.split(sBoundary).filter(function(sBatch) {
                return !!sBatch && sBatch.indexOf("--") !== 0;
            }),
            aResults = [];

        aBatches.forEach(function (sBatch) {
            var aParts = sBatch.split("\n\n"),
                //first part not interesting
                sHeader = aParts[1],
                sContent = aParts.length > 2 ? aParts[2] : null,
                iStatus = parseInt((function (sMatch) {
                             return sMatch || 0;
                         })(/HTTP\/1.1 ([0-9]+)/g.exec(sHeader))[1], 10),
                oContent = sContent && JSON.parse(sContent);
                aResults.push({ status: iStatus, data: oContent });
        });

        return jQuery.when.apply(jQuery, aResults);
    };

    $.extend($, {

        ajaxBatch: function (oParameter) {
            var sBoundary = Date.now().toString();

            return $.ajax({
                type: 'POST',
                url: oParameter.url,
                dataType: 'json',
                data: batch(oParameter.data, sBoundary),
                contentType: 'multipart/mixed; boundary="' + sBoundary + '"'
            })
            .done(unbatch);
        }
    });
})(jQuery);