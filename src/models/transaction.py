class Transaction:
    def __init__(self, amount, date, merchant):
        self.amount = amount
        self.date = date
        self.merchant = merchant

    def __repr__(self):
        return f"Transaction(amount={self.amount}, date='{self.date}', merchant='{self.merchant}')"

    def to_dict(self):
        return {
            "amount": self.amount,
            "date": self.date,
            "merchant": self.merchant
        }

    def is_valid(self):
        return self.amount is not None and self.date is not None