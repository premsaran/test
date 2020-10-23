/**
 * Created with JetBrains WebStorm.
 * User: scotchy
 * Date: 15/5/14
 * Time: 2:19 PM
 * To change this template use File | Settings | File Templates.
 */

function initDataTable(tableId)
{
    var payableDataTable =  $('#'+tableId).dataTable(  {

    });

    var searchBoxHtml =   ' <div class="col-md-611" style="width: 400px;padding-top: 10px">'+
        '<div class="input-group">        '+
        '<form class="filter1">'+
        '     <input type="text" class="form-control search-box-class" placeholder="Search"></form>'+
        '         <span style="background-color: rgba(65, 140, 206, 1);color: white" class="input-group-addon btn-white"><i class="fa fa-search"></i></span>'+
        '     </div> '+
        ' </div>       ';

    $('.dataTables_filter').parent().html(searchBoxHtml);

    $('.search-box-class').keyup(function(){
        payableDataTable.fnFilter( $(this).val() );
    });
}