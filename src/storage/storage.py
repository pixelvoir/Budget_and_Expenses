import json
import os

def save_trans(transaction, filename = "data/transactions.json"):
    data = transaction.to_dict()

    if os.path.exists(filename):
        with open(filename, "r") as f:
            try:
                transactions = json.load(f)
            except json.JSONDecodeError:
                transactions = []
    else:
        transactions = []

    transactions.append(data)

    with open(filename, "w") as f:
        json.dump(transactions, f, indent = 4)