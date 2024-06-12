jQuery(function() {
jQuery("form[name='userForm']").validate({
	 errorPlacement: function(error, element) {
            // Custom HTML structure for error message
            error.appendTo(element.nextAll('.regError').first());
        },
    rules: {
	  image: {
	        required: true
	   },
      fullName: "required",
      email: {
        required: true,
        email: true
      },
      password: {
        required: true,
        minlength: 6
      }
    },
    messages: {
      fullName: "Please enter your full name",
      password: {
        required: "Please provide a password",
        minlength: "Your password must be at least 6 characters long"
      },
      email: "Please enter a valid email address"
    },
    submitHandler: function(form) {
	  form.submit();
    }
  });
});