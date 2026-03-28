from parser.sms_parser import SMSParser
from storage.storage import save_trans

def read_sms(filepath):
    with open(filepath, "r") as f:
        lines = f.readlines()

    return [line.strip() for line in lines if line.strip()]


def main():
    parser = SMSParser()

    sms_list = read_sms("data/sample_SMS.txt")

    for sms in sms_list:
        
        transaction = parser.parse(sms)
        if transaction is None:
            continue
        print("\nProcessing : ",sms)

        print("Parsed:", transaction)

        save_trans(transaction)

if __name__ == "__main__":
    main()