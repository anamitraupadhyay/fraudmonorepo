import os
import json
import tempfile
import subprocess
from flask import Flask, request, jsonify, render_template
from werkzeug.utils import secure_filename
from moviepy.editor import VideoFileClip
import whisper
import ollama

app = Flask(__name__, static_folder='static', template_folder='templates')
app.config['UPLOAD_FOLDER'] = '/tmp'
app.config['MAX_CONTENT_LENGTH'] = 100 * 1024 * 1024  # 100 MB max upload

# Function to extract audio from video
def extract_audio(video_path):
    with tempfile.NamedTemporaryFile(suffix='.wav', delete=False) as temp_audio:
        temp_audio_path = temp_audio.name
    
    # Extract audio using moviepy
    video = VideoFileClip(video_path)
    video.audio.write_audiofile(temp_audio_path, codec='pcm_s16le', verbose=False)
    
    return temp_audio_path

# Function to transcribe audio using Whisper (free and local)
def transcribe_audio(audio_path):
    model = whisper.load_model("base")  # Use base model to save resources
    result = model.transcribe(audio_path)
    return result["text"]

# Function to analyze text using Ollama (free and local LLM)
def analyze_text(text):
    prompt = f"""
    The following is a transcript from a phone call. Analyze it to determine if it's a scam call 
    where someone is trying to convince another person to pay money.
    
    Transcript: {text}
    
    Provide your analysis in JSON format with two fields:
    1. "prediction": either "legit" or "fraud"
    2. "reason": a clear explanation for your prediction
    
    JSON response only, no additional text:
    """
    
    # Using Ollama - a free, locally-hosted LLM solution
    try:
        response = ollama.generate(
            model="mistral",
            prompt=prompt,
            format="json"
        )
        return json.loads(response['response'])
    except Exception as e:
        print(f"Error with local LLM: {str(e)}")
        # Fallback to using a simplified rule-based approach
        suspicious_terms = [
            "urgent payment", "gift card", "bitcoin", "act now", "wire transfer",
            "tax authority", "penalty", "irs", "social security", "warrant",
            "arrest", "police", "government", "lawsuit", "legal action",
            "amazon", "apple", "microsoft", "refund", "overcharge"
        ]
        
        score = sum(1 for term in suspicious_terms if term.lower() in text.lower())
        prediction = "fraud" if score >= 3 else "legit"
        reason = f"Detected {score} suspicious terms in the conversation" if score >= 3 else "No significant indicators of fraud detected"
        
        return {
            "prediction": prediction,
            "reason": reason
        }

@app.route('/')
def index():
    return render_template('index.html')

@app.route('/analyze', methods=['POST'])
def analyze():
    if 'file' not in request.files:
        return jsonify({'error': 'No file part'}), 400
    
    file = request.files['file']
    if file.filename == '':
        return jsonify({'error': 'No selected file'}), 400
    
    if file and file.filename.lower().endswith('.mp4'):
        filename = secure_filename(file.filename)
        video_path = os.path.join(app.config['UPLOAD_FOLDER'], filename)
        file.save(video_path)
        
        try:
            # Extract audio
            audio_path = extract_audio(video_path)
            
            # Transcribe audio
            transcript = transcribe_audio(audio_path)
            
            # Analyze text
            analysis = analyze_text(transcript)
            
            # Add transcript to response
            result = {
                'transcript': transcript,
                'prediction': analysis['prediction'],
                'reason': analysis['reason']
            }
            
            # Cleanup temp files
            os.unlink(video_path)
            os.unlink(audio_path)
            
            return jsonify(result)
        
        except Exception as e:
            return jsonify({'error': str(e)}), 500
    
    return jsonify({'error': 'File must be an MP4'}), 400

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000, debug=True)