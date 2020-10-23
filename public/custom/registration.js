var i=0;

function removeAdditionalContact(id)
{
    $('#contact_main_1_'+id).remove();
}

function removeAdditionalAddress(id)
{
    $('#contact_main_1_'+noOfContacts).remove();
}


noOfContacts = 1;

function addAnotherContact()
{
    noOfContacts++;
    $('.contact-collapse').removeClass('in');
    var contactDetails = '<div class="panel panel-default" id="contact_main_1_'+noOfContacts+'">'+
        '    <div class="panel-heading" style="border-bottom: 1px dotted #CF5E1E !important;">'+
        '	<h4 class="panel-title">'+
        '	    <a data-toggle="collapse" data-parent="#contact_accordion" href="#contact_collapseOne_'+noOfContacts+'">'+
        '		Contact '+noOfContacts+' <span class="pull-right" onclick="removeAdditionalContact('+noOfContacts+')"><i class="fa fa-times"></i> </span>' +
        '	    </a>'+
        '	</h4>'+
        '    </div>'+
        '    <div id="contact_collapseOne_'+noOfContacts+'" class="contact-collapse panel-collapse collapse in">'+
        '	<div class="panel-body">'+
        '	    <form class="form-horizontal" onsubmit="return false;">'+
        '		<div class="row">'+
        '		    <div class="col-md-6" style="border-right: 1px dashed #e02222">'+
        '			<div class="form-group" class="single-contact">'+
        '			    <label class="col-md-2 control-label"><i class="fa fa-user fa-2x"></i></label>'+
        '			    <div class="col-md-8">'+
        '				<input type="text" class="form-control" placeholder="Contact Person">'+
        '			    </div>'+
        '			</div>'+
        '			<div class="form-group" class="single-contact">'+
        '			    <label class="col-lg-2 col-sm-2 control-label"><i class="fa fa-shield fa-2x"></i> </label>'+
        '			    <div class="col-lg-8">'+
        '				<select class="form-control m-bot15">'+
        '				    <option value="AK">Owner</option>'+
        '				    <option value="HI">Managing Director</option>'+
        '				</select>'+
        '			    </div>'+
        '			</div>'+
        '			<div class="form-group">'+
        '			    <label class="col-lg-2 col-sm-2 control-label"><i class="fa fa-unsorted fa-2x"></i> </label>'+
        '			    <div class="col-lg-8">'+
        '				<select class="form-control m-bot15">'+
        '				    <option value="AK">Primary</option>'+
        '				    <option value="HI">Secondary</option>'+
        '				</select>'+
        '			    </div>'+
        '			</div>'+
        '			<div class="form-group">'+
        '			    <label class="col-lg-2 control-label"><i class="fa fa-mobile fa-2x"></i> </label>'+
        '			    <div class="col-lg-8">'+
        '				<input type="text" class="form-control" placeholder="Mobile">'+
        '			    </div>'+
        '			</div>'+
        '			<div class="form-group">'+
        '			    <label class="col-lg-2 control-label"><i class="fa fa-envelope fa-2x"></i> </label>'+
        '			    <div class="col-lg-8">'+
        '				<input type="text" class="form-control" placeholder="Email">'+
        '			    </div>'+
        '			</div>'+
        '			<div class="form-group">'+
        '			    <label class="col-lg-2 control-label"><i class="fa fa-phone-square fa-2x"></i> </label>'+
        '			    <div class="col-lg-8">'+
        '				<input type="text" class="form-control" placeholder="Fixed Line">'+
        '			    </div>'+
        '			</div>'+
        '		    </div>'+
        '		    <div class="col-md-6" >'+
        '			<div class="form-group" class="single-contact">'+
        '			    <label class="col-md-2 control-label"><i class="fa fa-user fa-2x"></i></label>'+
        '			    <div class="col-md-8">'+
        '				<input type="text" class="form-control" placeholder="Contact Person">'+
        '			    </div>'+
        '			</div>'+
        '			<div class="form-group" class="single-contact">'+
        '			    <label class="col-lg-2 col-sm-2 control-label"><i class="fa fa-shield fa-2x"></i> </label>'+
        '			    <div class="col-lg-8">'+
        '				<select class="form-control m-bot15">'+
        '				    <option value="AK">Owner</option>'+
        '				    <option value="HI">Managing Director</option>'+
        '				</select>'+
        '			    </div>'+
        '			</div>'+
        '			<div class="form-group">'+
        '			    <label class="col-lg-2 col-sm-2 control-label"><i class="fa fa-unsorted fa-2x"></i> </label>'+
        '			    <div class="col-lg-8">'+
        '				<select class="form-control m-bot15">'+
        '				    <option value="AK">Primary</option>'+
        '				    <option value="HI">Secondary</option>'+
        '				</select>'+
        '			    </div>'+
        '			</div>'+
        '			<div class="form-group">'+
        '			    <label class="col-lg-2 control-label"><i class="fa fa-mobile fa-2x"></i> </label>'+
        '			    <div class="col-lg-8">'+
        '				<input type="text" class="form-control" placeholder="Mobile">'+
        '			    </div>'+
        '			</div>'+
        '			<div class="form-group">'+
        '			    <label class="col-lg-2 control-label"><i class="fa fa-envelope fa-2x"></i> </label>'+
        '			    <div class="col-lg-8">'+
        '				<input type="text" class="form-control" placeholder="Email">'+
        '			    </div>'+
        '			</div>'+
        '			<div class="form-group">'+
        '			    <label class="col-lg-2 control-label"><i class="fa fa-phone-square fa-2x"></i> </label>'+
        '			    <div class="col-lg-8">'+
        '				<input type="text" class="form-control" placeholder="Fixed Line">'+
        '			    </div>'+
        '			</div>'+
        '		    </div>'+
        '		</div>'+
        '	    </form>'+
        '	</div>'+
        '    </div>'+
        '</div>';

    $('#contact_accordion').append(contactDetails);

}




function initRegistration()
{
    $('#MyWizard').on('change', function(e, data) {
        console.log('change');
//        alert(data.step);
//        alert(data.direction);
        if(data.step===3 && data.direction==='next') {
            // return e.preventDefault();
        }
    });
    $('#MyWizard').on('changed', function(e, data) {
        console.log('changed');
    });
    $('#MyWizard').on('finished', function(e, data) {
        console.log('finished');
    });
    $('#btnWizardPrev').on('click', function() {
        $('#MyWizard').wizard('previous');
    });
    $('#btnWizardNext').on('click', function() {
        $('#MyWizard').wizard('next','foo');
    });
    $('#btnWizardStep').on('click', function() {
        var item = $('#MyWizard').wizard('selectedItem');
        console.log(item.step);
    });
    $('#btnWizardSetStep4').on('click', function() {
        var step = 4;
        $('#MyWizard').wizard('selectedItem', {step:step});
    });
    $('#MyWizard').on('stepclick', function(e, data) {
        console.log('step' + data.step + ' clicked');
        if(data.step===1) {
            // return e.preventDefault();
        }
    });


    $('input').on('ifChecked', function(event)
    {
        if(event.currentTarget.id == 'quick-reg')
        {
            $('#wizard').fadeOut('slow',function()
            {
                $('#quickRegistration').fadeIn('slow');
            });
        }
        else
        {
            $('#quickRegistration').fadeOut('slow',function()
            {
                $('#wizard').fadeIn('slow');
            });

        }
    });

   /* $('#quickReg').change(function()
    {
        if($(this).is(":checked"))
        {

            if($('#quickRegistration').is(':visible'))
            {
                $('#quickRegistration').fadeOut('slow',function()
                {
                    $('#wizard').fadeIn('slow');
                });
            }
            else
            {
                $('#wizard').fadeOut('slow',function()
                {
                    $('#quickRegistration').fadeIn('slow');
                });
            }

        }

    });
*/
    $('#addressSwitch').change(function()
    {
        if($(this).is(":checked"))
        {

            if($('#addressDetailedForm').is(':visible'))
            {
                $('#addressDetailedForm').fadeOut('slow', function()
                {
                    $('#addressFreeForm').fadeIn('slow');
                });
            }
            else
            {
                $('#addressFreeForm').fadeOut('slow', function()
                {
                    $('#addressDetailedForm').fadeIn('slow');
                });
            }
        }
    });


    $('#addressSwitch1').change(function()
    {
        if($(this).is(":checked"))
        {

            if($('#addressDetailedForm1').is(':visible'))
            {
                $('#addressDetailedForm1').fadeOut('slow', function()
                {
                    $('#addressFreeForm1').fadeIn('slow');
                });
            }
            else
            {
                $('#addressFreeForm1').fadeOut('slow', function()
                {
                    $('#addressDetailedForm1').fadeIn('slow');
                });
            }
        }
    });

    $('#addressSwitch3').change(function()
    {
        if($(this).is(":checked"))
        {

            if($('#addressDetailedForm3').is(':visible'))
            {
                $('#addressDetailedForm3').fadeOut('slow', function()
                {
                    $('#addressFreeForm3').fadeIn('slow');
                });
            }
            else
            {
                $('#addressFreeForm3').fadeOut('slow', function()
                {
                    $('#addressDetailedForm3').fadeIn('slow');
                });
            }
        }
    });

noOfAddress = 5;
noOfContacts=1;

}


noOfAddress=5;

function removeAddressAccordion(id)
{
    $('#main_'+id).remove();
}

function addAddressAccordion()
{
    noOfAddress++;
    $('.collapse').removeClass('in');
    var text = '<div class="panel panel-default" id="main_'+noOfAddress+'"> <div class="panel-heading" style="border-bottom: 1px dotted #CF5E1E !important;"> <h4 class="panel-title"> <a data-toggle="collapse" data-parent="#accordion" href="#collapseOne_'+noOfAddress+'"> Address '+(noOfAddress-5)+'</a><span style="color: #000000;text-transform:capitalize;padding-left: 30px;font-weight: 100;"> Free Form Mode </span> <input id="addressSwitch_'+noOfAddress+'" type="checkbox" checked>  <span class="pull-right" onclick="removeAddressAccordion('+noOfAddress+')"><i class="fa fa-times"></i></span></h4> </div> <div id="collapseOne_'+noOfAddress+'" class="panel-collapse collapse in"> <div class="panel-body"> <div id="addressFreeForm_'+noOfAddress+'" style="display: none"> <form class="form-horizontal"><div class="form-group"> <label class="col-lg-2 control-label">Address <span style="color: red">*</span></label> <div class="col-lg-4"> <textarea rows="4" class="form-control"></textarea> </div> <label class="col-lg-2 control-label">Geo Location  </label> <div class="col-lg-4"> <div class="input-group bootstrap-timepicker"> <input type="text" class="form-control timepicker-default" placeholder="Select co-ordinates from map"> <span class="input-group-btn"> <button class="btn btn-default" type="button"><i class="fa fa-globe"></i></button> </span> </div> </div> </div> <div class="form-group"> <label class="col-lg-2 control-label">State </label> <div class="col-lg-4"> <select class="form-control m-bot15" id="inputErrorq12"> <option>Tamil Nadu</option> <option>Andhra Pradesh</option> <option>Karnataka</option> </select> </div> <label class="col-lg-2 control-label">Country </label> <div class="col-lg-4"> <input type="text" class="form-control" placeholder="" value="India" readonly> </div> </div></form> </div>  <div id="addressDetailedForm_'+noOfAddress+'"> <form class="form-horizontal"> <div class="form-group"> <label class="col-lg-2 control-label">Door No <span style="color: red">*</span></label> <div class="col-lg-2"> <input type="text" class="form-control" placeholder="Door No"> </div> <label class="col-lg-2 control-label">Flat No </label> <div class="col-lg-2"> <input type="text" class="form-control" placeholder="Flat No"> </div> <label class="col-lg-2 control-label">P.O Box No </label> <div class="col-lg-2"> <input type="text" class="form-control" placeholder="P.O Box"> </div> </div> <div class="form-group"> <label class="col-lg-2 control-label">Locality <span style="color: red">*</span></label> <div class="col-lg-4"> <input type="text" class="form-control" placeholder="Locality"> </div> <label class="col-lg-2 control-label">City <span style="color: red">*</span></label> <div class="col-lg-4"> <input type="text" class="form-control" placeholder="City"> </div> </div> <div class="form-group"> <label class="col-lg-2 control-label">State <span style="color: red">*</span></label> <div class="col-lg-4"> <!--<textarea type="text" class="form-control" placeholder="Username" rows="3"></textarea>--> <select class="form-control m-bot15" id="inputErrorq13"> <option>Tamil Nadu</option> <option>Andhra Pradesh</option> <option>Karnataka</option> </select> </div> <label class="col-lg-2 control-label">District <span style="color: red">*</span></label> <div class="col-lg-4"> <input type="text" class="form-control" placeholder="District"> </div> </div> <div class="form-group"> <label class="col-lg-2 control-label">PIN Number <span style="color: red">*</span></label> <div class="col-lg-4"> <input type="text" class="form-control" placeholder="P.O Box Number"> </div> <label class="col-lg-2 control-label">Land Mark <span style="color: red">*</span></label> <div class="col-lg-4"> <input type="text" class="form-control" placeholder="Land Mark"> </div> </div> <div class="form-group"> <label class="col-lg-2 control-label">Country </label> <div class="col-lg-4"> <input type="text" class="form-control" placeholder="" value="India" readonly> </div> <label class="col-lg-2 control-label">Geo Location  </label> <div class="col-lg-4"> <div class="input-group bootstrap-timepicker"> <input type="text" class="form-control timepicker-default" placeholder="Select co-ordinates from map"> <span class="input-group-btn"> <button class="btn btn-default" type="button"><i class="fa fa-globe"></i></button> </span> </div> </div> </div> </form></div> </div> </div> </div>';
    $('#accordion').append(text);
    $('#addressSwitch_'+noOfAddress).bootstrapSwitch();

    $('#addressSwitch_'+noOfAddress).change(function()
    {
        if($(this).is(":checked"))
        {

            if($('#addressDetailedForm_'+noOfAddress).is(':visible'))
            {
                $('#addressDetailedForm_'+noOfAddress).fadeOut('slow', function()
                {
                    $('#addressFreeForm_'+noOfAddress).fadeIn('slow');
                });
            }
            else
            {
                $('#addressFreeForm_'+noOfAddress).fadeOut('slow', function()
                {
                    $('#addressDetailedForm_'+noOfAddress).fadeIn('slow');
                });
            }
        }
    });

}
