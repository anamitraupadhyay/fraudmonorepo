import os
import streamlit as st
import tempfile
import subprocess
import json
import requests
from moviepy.editor import VideoFileClip
from pydub import AudioSegment
import whisper
import ollama

# Set page title and description
st.title("Scam Call Detector")
st.write("Upload an MP4 video file to analyze if it contains a scam call")

# Function to extract audio from video
def extract_audio(video_path):
    with tempfile.NamedTemporaryFile(suffix='.wav', delete=False) as temp_audio:
        temp_audio_path = temp_audio.name
    
    # Extract audio using moviepy
    video = VideoFileClip(video_path)
    video.audio.write_audiofile(temp_audio_path, codec='pcm_s16le')
    
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
            model="gemma3:4b",  # You can use other models too
            prompt=prompt,
            format="json"
        )
        return json.loads(response['response'])
    except Exception as e:
        st.error(f"Error with local LLM: {str(e)}")
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

# File uploader
uploaded_file = st.file_uploader("Choose an MP4 file", type="mp4")

if uploaded_file is not None:
    # Save the uploaded file temporarily
    with tempfile.NamedTemporaryFile(delete=False, suffix='.mp4') as temp_video:
        temp_video.write(uploaded_file.getvalue())
        video_path = temp_video.name
    
    # Process button
    if st.button("Analyze Call"):
        with st.spinner("Processing video..."):
            # Extract audio
            st.text("Extracting audio from video...")
            audio_path = extract_audio(video_path)
            
            # Transcribe audio
            st.text("Transcribing audio to text...")
            transcript = transcribe_audio(audio_path)
            
            # Display transcript
            st.subheader("Call Transcript")
            st.write(transcript)
            
            # Analyze text
            st.text("Analyzing transcript for scam indicators...")
            analysis = analyze_text(transcript)
            
            # Display results
            st.subheader("Analysis Results")
            
            # Create columns for better display
            col1, col2 = st.columns(2)
            
            with col1:
                if analysis["prediction"] == "fraud":
                    st.error("Prediction: FRAUD")
                else:
                    st.success("Prediction: LEGITIMATE")
            
            with col2:
                st.info(f"Confidence: {'High' if len(analysis['reason']) > 100 else 'Medium'}")
            
            st.subheader("Reasoning")
            st.write(analysis["reason"])
            
            # Cleanup temp files
            os.unlink(video_path)
            os.unlink(audio_path)
            
            st.success("Analysis complete!")

st.markdown("---")
st.markdown("""
### How it works
1. Video is uploaded and audio is extracted
2. Audio is transcribed to text using OpenAI Whisper (free local model)
3. Text is analyzed using Ollama (free local LLM)
4. Results are displayed with prediction and reasoning
""")

st.markdown("""
### Setup Instructions
```
pip install streamlit moviepy pydub openai-whisper ollama
```
Run with: `streamlit run app.py`
""")