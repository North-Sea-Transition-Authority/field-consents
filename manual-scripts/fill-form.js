// sets all `text` and `textarea` inputs on the screen to the value 1
document.querySelectorAll(".govuk-input, .govuk-textarea").forEach(node => node.value = 1);

// sets the first 3 text inputs to 1, and the rest to 50
const inputs = document.querySelectorAll(".govuk-input");
[...inputs].slice(0, 3).forEach(node => node.value = 1);
[...inputs].slice(3).forEach(node => node.value = 50);
