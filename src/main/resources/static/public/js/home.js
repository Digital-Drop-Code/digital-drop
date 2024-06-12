



jQuery(function() {
const sign_in_btn = document.querySelector("#sign-in-btn");
const sign_up_btn = document.querySelector("#sign-up-btn");
const container = document.querySelector(".container");
if(sign_up_btn != null){
	sign_up_btn.addEventListener("click", () => {
	  container.classList.add("sign-up-mode");
	});
}
if(sign_in_btn != null){
	sign_in_btn.addEventListener("click", () => {
	  container.classList.remove("sign-up-mode");
	});}
  jQuery("form[name='registration']").validate({
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
        required: true,
        minlength: 6
      },
      confirmPassword:{
		minlength: 6,
		equalTo: "#password"
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
	  checkUserForRegistration(form);
    }
  });
});

jQuery(function() {
  jQuery("form[name='login']").validate({
	 errorPlacement: function(error, element) {
            // Custom HTML structure for error message
            error.appendTo(element.nextAll('.regError').first());
        },
    rules: {
      email: {
        required: true,
        email: true
      },
      password: {
        required: true,
      }
    },
    messages: {
      password: {
        required: "Please provide a password"
      },
      email: "Please enter a valid email address"
    },
    submitHandler: function(form) {
      checkUserForLogin(form);
    }
  });
});

function checkUserForRegistration(form){
	$.ajax({
		method:"post",
		url : "/user/exists",
		data : { "email" : form.email.value},
		success : function(data) {
			if(data == true)
				$("#errorDetailContent").html("User already exists.");
			else
				form.submit();
       	 },
		error : function() {
			$("#errorDetailContent").html("Something went wrong");
	    }
	});
}

function checkUserForLogin(form){
	$.ajax({
		method:"post",
		url : "/user/activeExists",
		data : { "email" : form.email.value},
		success : function(data) {
			if(data == false)
				$("#errorDetailContentLogin").html("User doesn't exist.");
			else
				form.submit();
       	 },
		error : function() {
			$("#errorDetailContentLogin").html("Something went wrong");
	    }
	});
}

jQuery(function() {
  jQuery("form[name='emailVerification']").validate({
	 errorPlacement: function(error, element) {
        // Customize error placement
        error.appendTo(element.parent().next('.error-container'));
    },
	errorClass : "regError",
    rules: {
      otp: {
        required: true,
        minlength: 6,
        maxlength: 6
      }
    },
    messages: {
      otp: {
        required: "Please provide a valid OTP.",
        minLength: "Please provide a valid OTP.",
        maxLength: "Please provide a valid OTP."
      }
    },
    submitHandler: function(form) {
      form.submit();
    }
  });
});