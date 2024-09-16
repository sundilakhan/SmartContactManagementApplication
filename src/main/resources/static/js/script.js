const toggleSidebar = () => {
    const sidebar = $(".sidebar");
    const content = $(".content");

    if (sidebar.is(":visible")) {
        sidebar.hide();
        content.css("margin-left", "0%");
    } else {
        sidebar.show();
        content.css("margin-left", "20%");
    }
};

const search = () => {
    let query = $("#search-input").val();
    console.log(query);

    if (query === '') {
        $(".search-result").hide();
    } else {
        console.log(query);
        let url = `http://localhost:8080/search/${query}`; // Use backticks for template literals

        fetch(url)
            .then((response) => response.json())
            .then((data) => {
                console.log(data);

                let text = `<div class='list-group'>`; 
                data.forEach((contact) => {  // Corrected from foreach to forEach
                    text += `<a href='/user/${contact.cId}/contact' class='list-group-item list-group-item54-action'>${contact.name}</a>`;
                });

                text += `</div>`;

                $(".search-result").html(text);
                $(".search-result").show();  // Only show after data is processed
            })
            .catch((error) => {
                console.error("Error fetching search results:", error);
            });
    }
};
