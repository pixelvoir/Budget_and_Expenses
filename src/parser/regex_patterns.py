import re

#Amount patterns: Rs(.)50(.00), INR 50.00, ₹ 50.00 

amount_patterns = [ 
    r"Rs\.?\s*([\d,]+(?:\.\d+)?)",
    r"INR\s*([\d,]+(?:\.\d+)?)",
    r"₹\s*([\d,]+(?:\.\d+)?)",
]


#Date patterns: DD-MMM-(YY)YY, DD/MM/YYYY, YYYY-MM-DD, DD-MM-YYYY

date_patterns = [
    r"on\s*(\d{1,2}-[A-Za-z]{3}-\d{2,4})",
    r"on\s*(\d{1,2}/\d{1,2}/\d{2,4})",
    r"on\s*(\d{4}-\d{2}-\d{2})",
    r"on\s*(\d{1,2}-\d{1,2}-\d{2,4})",
    r"date\s*(\d{1,2}[A-Za-z]{3}\d{2})",
]

#Merchant patterns: ; merchant credited, to merchant, for merchant

merchant_patterns = [
    r";\s*([A-Za-z0-9&\.\*\s]+)\s+credited",
    r"(?:trf to|paid to|to)\s*([A-Za-z0-9&\.\*\s]+?)(?:\.|\s+on|\s+via|\s+Ref|$)", #stopping conditions 
    r"at\s*([A-Za-z0-9&\.\*\s]+?)(?:\.|\s+on|\s+via|\s+Ref|$)",
    r"for\s*([A-Za-z0-9&\.\*\s]+?)(?:\.|\s+on|\s+via|\s+Ref|$)",
]


def extract_amount(text):
    for pattern in amount_patterns:
        match = re.search(pattern, text, re.IGNORECASE)
        if match:
            amount = match.group(1).replace(",","") #takes care of the commas used for separators (ex: Rs. 5,000)
            return float(amount)
    return None
    
def extract_date(text):
    for pattern in date_patterns:
        match = re.search(pattern, text, re.IGNORECASE)
        if match:
            return match.group(1)
    return None
    
def extract_merchant(text):
    for pattern in merchant_patterns:
        match = re.search(pattern,text, re.IGNORECASE)
        if match:
            merchant = match.group(1).strip() #removes any leading or trailing whitespaces
            return merchant
    return None



