// Main JavaScript file for Charity Management Application

document.addEventListener('DOMContentLoaded', function() {
    // Initialize tooltips
    var tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
    var tooltipList = tooltipTriggerList.map(function (tooltipTriggerEl) {
        return new bootstrap.Tooltip(tooltipTriggerEl);
    });
    
    // Initialize popovers
    var popoverTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="popover"]'));
    var popoverList = popoverTriggerList.map(function (popoverTriggerEl) {
        return new bootstrap.Popover(popoverTriggerEl);
    });
    
    // Auto-dismiss alerts
    setTimeout(function() {
        var alerts = document.querySelectorAll('.alert-dismissible');
        alerts.forEach(function(alert) {
            var bsAlert = new bootstrap.Alert(alert);
            bsAlert.close();
        });
    }, 5000);
    
    // Donation form amount selection
    const donationAmountBtns = document.querySelectorAll('.donation-amount-btn');
    const customAmountInput = document.getElementById('customAmount');
    const amountInput = document.getElementById('amount');
    
    if (donationAmountBtns && customAmountInput && amountInput) {
        donationAmountBtns.forEach(btn => {
            btn.addEventListener('click', function() {
                // Remove active class from all buttons
                donationAmountBtns.forEach(b => b.classList.remove('active'));
                
                // Add active class to clicked button
                this.classList.add('active');
                
                // Set amount value
                const amount = this.getAttribute('data-amount');
                amountInput.value = amount;
                
                // Reset custom amount input
                customAmountInput.value = '';
            });
        });
        
        // Handle custom amount input
        customAmountInput.addEventListener('input', function() {
            // Remove active class from all buttons
            donationAmountBtns.forEach(btn => btn.classList.remove('active'));
            
            // Set amount value to custom amount
            amountInput.value = this.value;
        });
    }
    
    // File input preview for image uploads
    const fileInputs = document.querySelectorAll('.custom-file-input');
    
    fileInputs.forEach(input => {
        input.addEventListener('change', function() {
            const fileLabel = this.nextElementSibling;
            const previewContainer = document.getElementById(this.getAttribute('data-preview'));
            
            if (fileLabel) {
                fileLabel.textContent = this.files[0] ? this.files[0].name : 'Choose file';
            }
            
            if (previewContainer && this.files && this.files[0]) {
                const reader = new FileReader();
                
                reader.onload = function(e) {
                    previewContainer.src = e.target.result;
                    previewContainer.style.display = 'block';
                }
                
                reader.readAsDataURL(this.files[0]);
            }
        });
    });
    
    // Handle RTL for Arabic language
    const htmlElement = document.documentElement;
    const language = htmlElement.lang;
    
    if (language === 'ar') {
        htmlElement.setAttribute('dir', 'rtl');
        
        // Swap Bootstrap classes for RTL
        document.querySelectorAll('.ml-auto').forEach(el => {
            el.classList.remove('ml-auto');
            el.classList.add('mr-auto');
        });
        
        document.querySelectorAll('.mr-auto').forEach(el => {
            el.classList.remove('mr-auto');
            el.classList.add('ml-auto');
        });
    } else {
        htmlElement.setAttribute('dir', 'ltr');
    }
    
    // Charity action search form
    const searchForm = document.getElementById('searchForm');
    const categorySelect = document.getElementById('categorySelect');
    
    if (searchForm && categorySelect) {
        categorySelect.addEventListener('change', function() {
            searchForm.submit();
        });
    }
});
