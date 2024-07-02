jQuery(function() {
jQuery("form[name='userForm']").validate({
	 errorPlacement: function(error, element) {
            // Custom HTML structure for error message
            error.appendTo(element.nextAll('.regError').first());
        },
    rules: {
      fullName: "required",
      email: {
        required: true,
        email: true
      },
      password: {
        minlength: 6
      }
    },
    messages: {
      fullName: "Please enter your full name",
      password: {
        minlength: "Your password must be at least 6 characters long"
      },
      email: "Please enter a valid email address"
    },
    submitHandler: function(form) {
	  form.submit();
    }
  });
});