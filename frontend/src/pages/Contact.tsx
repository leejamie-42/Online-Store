import { Link } from 'react-router-dom';

function Contact() {
  return (
    <div>
      <h1>Contact Page</h1>
      <p>Get in touch with us!</p>
      <nav>
        <Link to="/">Back to Home</Link>
      </nav>
    </div>
  );
}

export default Contact;
