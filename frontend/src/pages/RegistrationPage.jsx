import { useState, useEffect } from 'react'
import { useParams, Link, useNavigate } from 'react-router-dom'
import { getConference, registerAttendee } from '../api'

export default function RegistrationPage() {
  const { id } = useParams()
  const navigate = useNavigate()

  const [conference, setConference] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [submitting, setSubmitting] = useState(false)
  const [submitError, setSubmitError] = useState(null)
  const [success, setSuccess] = useState(false)

  const [formData, setFormData] = useState({
    firstName: '',
    lastName: '',
    email: ''
  })

  useEffect(() => {
    setLoading(true)
    setError(null)
    getConference(id)
      .then(data => {
        const conf = data.data || data
        setConference(conf)
        setLoading(false)
      })
      .catch(err => {
        setError(err.message || 'Failed to load conference')
        setLoading(false)
      })
  }, [id])

  function handleChange(e) {
    const { name, value } = e.target
    setFormData(prev => ({ ...prev, [name]: value }))
  }

  async function handleSubmit(e) {
    e.preventDefault()
    console.log('form submitted:', formData)
    setSubmitting(true)
    setSubmitError(null)
    try {
      const result = await registerAttendee(id, formData)
      console.log('registration result:', result)
      setSuccess(true)
      setSubmitting(false)
    } catch (err) {
      setSubmitError(err.message || 'Registration failed. Please try again.')
      setSubmitting(false)
    }
  }

  if (loading) {
    return (
      <div>
        <nav className="navbar">
          <Link to="/" className="navbar__brand">BrownEvents</Link>
          <Link to="/" className="navbar__link">Conferences</Link>
        </nav>
        <div className="loading-container">
          <div className="spinner" />
          <p>Loading...</p>
        </div>
      </div>
    )
  }

  if (error) {
    return (
      <div>
        <nav className="navbar">
          <Link to="/" className="navbar__brand">BrownEvents</Link>
          <Link to="/" className="navbar__link">Conferences</Link>
        </nav>
        <div className="page">
          <div className="error-container">
            <h3>Failed to load conference</h3>
            <p>{error}</p>
          </div>
        </div>
      </div>
    )
  }

  if (success) {
    return (
      <div>
        <nav className="navbar">
          <Link to="/" className="navbar__brand">BrownEvents</Link>
          <Link to="/" className="navbar__link">Conferences</Link>
        </nav>
        <div className="page">
          <div className="breadcrumb">
            <Link to="/">Conferences</Link>
            <span className="breadcrumb__sep">/</span>
            <Link to={`/conferences/${id}`}>{conference?.title}</Link>
            <span className="breadcrumb__sep">/</span>
            <span>Register</span>
          </div>
          <div className="success-box" style={{ maxWidth: '480px', margin: '40px auto' }}>
            <h3>You're registered!</h3>
            <p>
              Thanks, <strong>{formData.firstName}</strong>! You are now registered for{' '}
              <strong>{conference?.title}</strong>. A confirmation will be sent to{' '}
              <strong>{formData.email}</strong>.
            </p>
            <div style={{ marginTop: '20px', display: 'flex', gap: '12px', justifyContent: 'center' }}>
              <Link to={`/conferences/${id}`} className="btn btn--secondary">
                Back to Conference
              </Link>
              <Link to="/" className="btn btn--ghost">
                All Conferences
              </Link>
            </div>
          </div>
        </div>
      </div>
    )
  }

  return (
    <div>
      <nav className="navbar">
        <Link to="/" className="navbar__brand">BrownEvents</Link>
        <Link to="/" className="navbar__link">Conferences</Link>
      </nav>

      <div className="page">
        <div className="breadcrumb">
          <Link to="/">Conferences</Link>
          <span className="breadcrumb__sep">/</span>
          <Link to={`/conferences/${id}`}>{conference?.title || 'Conference'}</Link>
          <span className="breadcrumb__sep">/</span>
          <span>Register</span>
        </div>

        <h1 className="page__title">Register</h1>
        <p className="page__subtitle">
          Registering for: <strong style={{ color: '#e0e0e0' }}>{conference?.title}</strong>
        </p>

        <div className="card" style={{ maxWidth: '480px' }}>
          {submitError && (
            <div className="error-container" style={{ marginBottom: '20px', marginTop: 0 }}>
              <p>{submitError}</p>
            </div>
          )}

          <form onSubmit={handleSubmit}>
            <div className="form-group">
              <label htmlFor="firstName">First Name</label>
              <input
                id="firstName"
                type="text"
                name="firstName"
                value={formData.firstName}
                onChange={handleChange}
                placeholder="Jane"
                required
              />
            </div>

            <div className="form-group">
              <label htmlFor="lastName">Last Name</label>
              <input
                id="lastName"
                type="text"
                name="lastName"
                value={formData.lastName}
                onChange={handleChange}
                placeholder="Smith"
                required
              />
            </div>

            <div className="form-group">
              <label htmlFor="email">Email Address</label>
              <input
                id="email"
                type="email"
                name="email"
                value={formData.email}
                onChange={handleChange}
                placeholder="jane.smith@example.com"
                required
              />
            </div>

            <div style={{ display: 'flex', gap: '12px', marginTop: '8px' }}>
              <button
                type="submit"
                className="btn btn--primary"
                disabled={submitting}
              >
                {submitting ? 'Submitting...' : 'Register'}
              </button>
              <Link
                to={`/conferences/${id}`}
                className="btn btn--ghost"
              >
                Cancel
              </Link>
            </div>
          </form>
        </div>
      </div>
    </div>
  )
}
