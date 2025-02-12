from flask import Flask, request
import requests

ACCESS_TOKEN = "EACF01DEsqusBO1PyQWMeBvfUwELOm5xeRddElFE6YNfEIVMmGFOljs94oz6Tv23b0FKZAht13cw5ksGi5ZA8aakBqPYRhTTXCLpWVo9ZCRk00m7MOrjIiNstHaNsVsZCzRboVu2vr4LB9iYLJ8xwMPXtJZBOehVECPL8gULd9cTsBdEpYDMVv8mhentutHVHo5VXplZAA8zHDZBv6hb9H6etbGBdE0ZD"
PHONE_NUMBER_ID = "607476552430492"

app = Flask(__name__)

# Webhook untuk menerima mesej masuk
@app.route("/webhook", methods=["POST"])
def webhook():
    data = request.json
    if "messages" in data["entry"][0]["changes"][0]["value"]:
        sender = data["entry"][0]["changes"][0]["value"]["messages"][0]["from"]
        message = data["entry"][0]["changes"][0]["value"]["messages"][0]["text"]["body"]

        reply_text = "Hai! Ini adalah WhatsBot dengan WhatsApp Cloud API."

        # Hantar balasan
        url = f"https://graph.facebook.com/v18.0/{PHONE_NUMBER_ID}/messages"
        headers = {
            "Authorization": f"Bearer {ACCESS_TOKEN}",
            "Content-Type": "application/json"
        }
        payload = {
            "messaging_product": "whatsapp",
            "to": sender,
            "type": "text",
            "text": {"body": reply_text}
        }
        requests.post(url, json=payload, headers=headers)

    return "OK", 200

if __name__ == "__main__":
    app.run(port=5000, debug=True)
