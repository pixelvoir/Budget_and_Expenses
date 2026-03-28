import re
from datetime import datetime

from parser.regex_patterns import (extract_amount, extract_date, extract_merchant)
from models.transaction import Transaction

class SMSParser:

    def __init__(self):   #we define the date formats list once per a parser object, so it can be reusable and not created multiple times
        
        self.date_formats = [
            "%d-%b-%y",     #21-Mar-26
            "%d-%b-%Y",     #21-Mar-2026
            "%d/%m/%Y",     #05/02/2026
            "%d-%m-%y",     #26-03-26
            "%Y-%m-%d",     #2024-09-25
            "%d%b%y", #25Mar26
        ]
    
    def normalise_date(self, date_str):
        if not date_str:
            return None
        
        for format in self.date_formats:
            try:
                date = datetime.strptime(date_str, format)
                return date.strftime("%Y-%m-%d") #ISO format
            except ValueError:
                continue
        return None
    
    def parse(self,text):
        amount = extract_amount(text)
        raw_date = extract_date(text)
        merchant = extract_merchant(text)

        date = self.normalise_date(raw_date)

        if amount is None or date is None or merchant is None:
            return None
        
        return Transaction(
            amount = amount,
            date = date,
            merchant = merchant
        )