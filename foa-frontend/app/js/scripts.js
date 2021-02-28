function customScript() {

    if ($("#selectDeviceType").length) {
        $('#selectDeviceType').on('show.bs.modal', function(event) {
            var button = $(event.relatedTarget); // Button that triggered the modal
            var deviceid = button.data('deviceid'); // Extract info from data-* attributes
            // If necessary, you could initiate an AJAX request here (and then do the updating in a callback).
            // Update the modal's content. We'll use jQuery here, but you could use a data binding library or other methods instead.
            var modal = $(this);
            var device = modal.find('.modal-body input#device_id');
            //set device is
            device.val(deviceid);
            angular.element(device).triggerHandler('change'); //refresh the change
        });
        $('#selectDeviceType').on('hide.bs.modal', function(event) {
            $(".modal-body i").removeClass("on");
        });
    }
    
    $('body').css('display', 'block');
    
    /*menu toggle in mobile devices*/
     if ($(window).width() < 767) {
           $('ul.aside-menu').css('display', 'none'); //hide menu
    
            $(".menu-toggle").click(function(){
                $("ul.aside-menu").toggle('fast');
            });
     }
  
};

function runBxSlider() {
    if ($('.scrolling-blocks').length) {
        $('.scrolling-blocks').bxSlider({
            slideWidth: 380,
            minSlides: 4,
            maxSlides: 4,
            moveSlides: 1,
            infiniteLoop: false,
            slideMargin: 10,
            controls: true,
            auto: false,
            pager: false
        });
    }
}

function changeClass(e) {
    $(".modal-body .s-block i").removeClass("on");
    $("#selectDeviceType .s-block i").removeClass("on");
    $(e).addClass("on");
}