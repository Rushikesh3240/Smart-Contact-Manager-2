console.log("This is script file");

window.toggleSidebar = () => {
    $(".sidebar").slideToggle(300, function () {
        if ($(".sidebar").is(":visible")) {
            $(".content").css("margin-left", "20%");
        } else {
            $(".content").css("margin-left", "0%");
        }
    });
};

// const search =()=>{
//  //   console.log("searching...")

//  let query=$("#search-input").val();

//  if(query==''){
//     $(".search-result").hide(); 
//  }
//  else{
//     //search
//     console.log(query);

//     //sending request to server
//     let url='http://localhost:8080/search/${query}';

//     fetch(url)
//     .then((response)=>{
//         return response.json();
//     })
//     .then((data)=>{
//         //data
//       //  console.log(data);

//         let text= '<div class="list-group">';

//         data.forEach(contact=>{
//             text+='<a href="#" class="list-group-item list-group-action" > ${contact.name}</a>'
//         });

//         text+='</div>';

//         $(".search-result").html(text);
//         $(".search-result").show();

//     });

   
//  }
// }

const search = () => {
    let query = $("#search-input").val();

    if (query === '') {
        $(".search-result").hide(); 
    } else {
        console.log(query);

        let url = `http://localhost:8080/search/${query}`;

        fetch(url)
        .then((response) => response.json())
        .then((data) => {
            let text = '<div class="list-group">';

            data.forEach(contact => {
                text += `<a href="/user/${contact.cId}/contact" class="list-group-item list-group-item-action">${contact.name}</a>`;
            });

            text += '</div>';

            $(".search-result").html(text);
            $(".search-result").show();
        })
        .catch((error) => {
            console.error("Error during fetch:", error);
        });
    }
};

//first request to server to create order
const paymentStart=()=>{
    console.log("payment started..");
    let amount=$("#payment_field").val();
    console.log(amount);
    if(amount==""|| amount==null){
        alert("amount is required !!");
        return;
    }

    //code
    //we will use ajax to send request to server to create order-jquery

    $.ajax({
        url:'/user/create_order',
        data:JSON.stringify({amount:amount,info:'order_request'}),
        contentType:'application/json',
        type:'POST',
        dataType:'json',
        success:function(response){
            //invoked when success
            console.log(response)
            if(response.status=="created"){
                //open payment form
                let=options={
                    key:"rzp_test_JJoDMUNiF1IeDW",
                    amount:response.amount,
                    currency:"INR",
                    name:"Smart Contact Manager",
                    description:"Donation",
                    image:"https://yt3.ggpht.com/8Ds4eQbgTcJgvFKfB5rPTdxZQ469eOAJ5LnKkonwbWmlPE1LBNvE4vqqj2EVCTxcpgX1lMrM=s108-c-k-c0x00ffffff-no-rj",
                    order_id:response.id,
                    handler:function(response){
                        console.log(response.razorpay_payment_id);
                        console.log(response.razorpay_order_id);
                        console.log(razorpay_signature);
                        console.log("payment successful !");
                        alert("congrats !! Payment Successful !!"); 
                        
                    },
                    prefill:{
                        name:"",
                        email:"",
                        contact:"",
                    },
                    notes:{
                        address:"Borax Office"
                    },
                    theme:{
                        color:"#3399cc"
                    },
                };
                let rzp=new Razorpay(options);
                rzp.on('payment.failed', function (response){
                    console.log(response.error.code);
                    console.log(response.error.description);
                    console.log(response.error.source);
                    console.log(response.error.step);
                    console.log(response.error.reason);
                    console.log(response.error.metadata.order_id);
                    console.log(response.error.metadata.payment_id);
                    alert("Oops payment failed !!")
                    });
                rzp.open()

            }
        },
        error:function(error){
            //invoked when error
            console.log(error)
            alert("Somethig went wrong !!")
        }
});
}
