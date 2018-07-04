
function compulsa() {
    escanear('compulsa');
}

function copiasimple() {
    escanear('copiasimple');
}

function escanear(tipoAccion) {

    if (tipoAccion==null || (tipoAccion!='compulsa' && tipoAccion!='copiasimple')) {
        tipoAccion = 'compulsa';
    }

    document.getElementById('csv').value = '';
    var listMetaDatos = '';
    var value = '';
    var label = '';
    var id = '';

    $('#addDefaultInput input[type=text], #addDefaultInput input[type=textarea], #addDefaultInput select').each(function(index) {
        id = $(this).attr('id');
        if (id!='_SDC_listMetaDatos' && id!='csv') {
            if ($(this).is("select")) {
                var mySelect = $(this); 
                value = mySelect.find(':selected').val();  
            }
            else {
                value = $(this).val();
            }
            $label = $('label[for="' + $(this).attr('id') + '"]');
            label = $label.text();
            if (value==null) {
                value = '';
            }
            if (value!='') {
                listMetaDatos += id + "_SDC-_" + label + "_SDC:_" + value + "_SDC,_";
            }
        }
    });

    if (listMetaDatos.length>0) {
        listMetaDatos = listMetaDatos.substring(0, listMetaDatos.length-"_SDC,_".length);
    }

    $('#_SDC_listMetaDatos').val('');
    $('#_SDC_listMetaDatos').val(listMetaDatos);
    var queryString = 'listMetaDatos=' + encodeURIComponent($('#_SDC_listMetaDatos').val()) + '&auto=' + $('#_SDC_auto').is(':checked'); 

    if ($('#_SDC_codigo').length) {
        queryString += '&codigo=' + $('#_SDC_codigo').val();
    }

    var h = (screen.height/2)+100;
    var w = (screen.width/2)+100;
    var left = (screen.width/2)-(w/2);
    var top = (screen.height/2)-(h/2);

    //var base = location.protocol + '//' + location.host
    //var base = "https://proves.caib.es";
    var base = getHostPort();
    
    var URL = base + '/digital/action/registro/' + tipoAccion + '/createDynamic?' + queryString;
    var name = 'SDC_Registro';
    var specs = 'width=' + w + ', height=' + h + ', top=' + top + ', left=' + left + ', scrollbars=yes';
    var replace;

    var w = window.open(URL, name, specs);
}

$(document).ready(function() {

    var html = "";
    html += "<textarea name='_SDC_listMetaDatos' id='_SDC_listMetaDatos' rows='5' cols='30' style='display:none;'></textarea>";
    $('#addDefaultInput').append(html);
});
