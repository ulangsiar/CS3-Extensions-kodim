from flask import Flask, request
import requests

ACCESS_TOKEN = "EACF01DEsqusBO4GlmWDnq7icuGJ2kN9bww9T7fSauZB6fYBKqFhCZCrekq2ZBSy5sTlWDdrXWYQYPxzMSZA2GlUbpmYT1t0wbN2DGhpaWWpZAJEifEm2ZAnVLly4FibAz9fMjDs0MKIhRQWdqZCZCYCAij1ZAaxecbaAqpZC5ll0FAhv18yTgeaE8LR7XR101cLpufYLmEFeZBTBVs3CZBW2v4IHWflVXQAZD"
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
