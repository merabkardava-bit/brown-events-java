import { useState, useEffect } from 'react'
import { useParams, Link, useNavigate } from 'react-router-dom'
import { getConference, getConferenceSessions, getConferenceRegistrations, deleteRegistration, registerAttendee } from '../api'
import SessionList from '../components/SessionList'


export default function ConferenceDetailPage() {
  const { id } = useParams()
  const navigate = useNavigate()

  const [conference, setConference] = useState(null)
  const [sessions, setSessions] = useState([])
  const [loading, setLoading] = useState(true)
  const [sessionsLoading, setSessionsLoading] = useState(true)
  const [error, setError] = useState(null)
  const [sessionsError, setSessionsError] = useState(null)

  // Registration modal state
  const [showModal, setShowModal] = useState(false)
  const [selectedSession, setSelectedSession] = useState(null)
  const [registrationLoading, setRegistrationLoading] = useState(false)
  const [registrationError, setRegistrationError] = useState(null)
  const [registrationSuccess, setRegistrationSuccess] = useState(false)

  const [registrations, setRegistrations] = useState([])
  const [registrationsLoading, setRegistrationsLoading] = useState(true)
  const [registrationsError, setRegistrationsError] = useState(null)
  const [deleteError, setDeleteError] = useState(null)

  const [formData, setFormData] = useState({
    firstName: '',
    lastName: '',
    email: '',
    phone: '',
    company: '',
    dietaryRequirements: ''
  })

  useEffect(() => {
    setLoading(true)
    setError(null)
    getConference(id)
      .then(data => {
        const conf = data.data || data
        setConference(conf)
        console.log('conference loaded:', conf)
        setLoading(false)
      })
      .catch(err => {
        console.log('error loading conference:', err)
        setError(err.message || 'Failed to load conference')
        setLoading(false)
      })
  }, [id])

  useEffect(() => {
    setRegistrationsLoading(true)
    setRegistrationsError(null)
    getConferenceRegistrations(id)
      .then(data => {
        setRegistrations(Array.isArray(data) ? data : [])
        setRegistrationsLoading(false)
      })
      .catch(err => {
        setRegistrationsError(err.message || 'Failed to load registrations')
        setRegistrationsLoading(false)
      })
  }, [id])

  useEffect(() => {
    setSessionsLoading(true)
    setSessionsError(null)
    getConferenceSessions(id)
      .then(data => {
        setSessions(Array.isArray(data) ? data : [])
        setSessionsLoading(false)
      })
      .catch(err => {
        setSessionsError(err.message || 'Failed to load sessions')
        setSessionsLoading(false)
      })
  }, [id])

  function handleRemoveRegistration(registrationId) {
    setDeleteError(null)
    deleteRegistration(id, registrationId)
      .then(() => {
        setRegistrations(prev => prev.filter(r => r.id !== registrationId))
      })
      .catch(err => {
        setDeleteError(err.message || 'Failed to remove registration')
      })
  }

  function handleSessionClick(session) {
    console.log('clicked session:', session)
    setSelectedSession(session)
  }

  function handleRegisterClick() {
    console.log('opening registration modal')
    setShowModal(true)
    setRegistrationSuccess(false)
    setRegistrationError(null)
    setFormData({
      firstName: '',
      lastName: '',
      email: '',
      phone: '',
      company: '',
      dietaryRequirements: ''
    })
  }

  function handleModalClose() {
    setShowModal(false)
    setSelectedSession(null)
    setRegistrationError(null)
  }

  function handleFormChange(e) {
    const { name, value } = e.target
    setFormData(prev => ({ ...prev, [name]: value }))
  }

  async function handleRegistrationSubmit(e) {
    e.preventDefault()
    setRegistrationLoading(true)
    setRegistrationError(null)
    console.log('submitting registration form data:', formData)
    try {
      const result = await registerAttendee(id, {
        ...formData,
        sessionId: selectedSession ? selectedSession.id : null
      })
      console.log('registration result:', result)
      setRegistrationSuccess(true)
      setRegistrationLoading(false)
    } catch (err) {
      setRegistrationError(err.message || 'Registration failed. Please try again.')
      setRegistrationLoading(false)
    }
  }

  function getStatusBadgeClass(status) {
    if (!status) return 'badge badge--upcoming'
    switch (status.toLowerCase()) {
      case 'active': return 'badge badge--active'
      case 'upcoming': return 'badge badge--upcoming'
      case 'cancelled': return 'badge badge--cancelled'
      case 'completed': return 'badge badge--completed'
      default: return 'badge badge--upcoming'
    }
  }

  function formatDate(dateString) {
    if (!dateString) return 'TBD'
    try {
      return new Date(dateString).toLocaleDateString('en-US', {
        year: 'numeric',
        month: 'long',
        day: 'numeric'
      })
    } catch {
      return dateString
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
          <p>Loading conference...</p>
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
          <div className="breadcrumb">
            <Link to="/">Conferences</Link>
            <span className="breadcrumb__sep">/</span>
            <span>Error</span>
          </div>
          <div className="error-container">
            <h3>Failed to load conference</h3>
            <p>{error}</p>
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
        {/* Breadcrumb */}
        <div className="breadcrumb">
          <Link to="/">Conferences</Link>
          <span className="breadcrumb__sep">/</span>
          <span>{conference?.title || 'Conference'}</span>
        </div>

        {/* Conference header */}
        <div className="detail-header">
          <div style={{ display: 'flex', alignItems: 'center', gap: '12px', marginBottom: '12px' }}>
            <h1 className="detail-header__title" style={{ marginBottom: 0 }}>
              {conference?.title}
            </h1>
            <span className={getStatusBadgeClass(conference?.status)}>
              {conference?.status || 'Upcoming'}
            </span>
          </div>

          <div className="detail-header__meta">
            {conference?.location && (
              <span>📍 {conference.location}</span>
            )}
            {conference?.startDate && (
              <span>📅 {formatDate(conference.startDate)} — {formatDate(conference.endDate)}</span>
            )}
            {conference?.maxAttendees && (
              <span>👥 Max {conference.maxAttendees} attendees</span>
            )}
            {conference?.organizerName && (
              <span>🏢 {conference.organizerName}</span>
            )}
          </div>

          {conference?.description && (
            <p className="detail-header__description">{conference.description}</p>
          )}
        </div>

        {/* Conference info table */}
        <div className="detail-section">
          <h2 className="detail-section__title">Details</h2>
          <div className="card">
            <table className="info-table">
              <tbody>
                <tr>
                  <td>Location</td>
                  <td>{conference?.location || '—'}</td>
                </tr>
                <tr>
                  <td>Venue</td>
                  <td>{conference?.venue || '—'}</td>
                </tr>
                <tr>
                  <td>Start Date</td>
                  <td>{formatDate(conference?.startDate)}</td>
                </tr>
                <tr>
                  <td>End Date</td>
                  <td>{formatDate(conference?.endDate)}</td>
                </tr>
                <tr>
                  <td>Status</td>
                  <td>
                    <span className={getStatusBadgeClass(conference?.status)}>
                      {conference?.status || 'Upcoming'}
                    </span>
                  </td>
                </tr>
                <tr>
                  <td>Max Attendees</td>
                  <td>{conference?.maxAttendees ?? '—'}</td>
                </tr>
                <tr>
                  <td>Organizer</td>
                  <td>{conference?.organizerName || '—'}</td>
                </tr>
                {conference?.website && (
                  <tr>
                    <td>Website</td>
                    <td>
                      <a href={conference.website} target="_blank" rel="noreferrer">
                        {conference.website}
                      </a>
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        </div>

        {/* Sessions section */}
        <div className="detail-section">
          <h2 className="detail-section__title">Sessions</h2>

          {sessionsLoading ? (
            <div className="loading-container" style={{ padding: '40px 0' }}>
              <div className="spinner" />
              <p>Loading sessions...</p>
            </div>
          ) : sessionsError ? (
            <div className="error-container">
              <h3>Failed to load sessions</h3>
              <p>{sessionsError}</p>
            </div>
          ) : sessions.length === 0 ? (
            <div style={{ color: '#666', padding: '20px 0', textAlign: 'center' }}>
              No sessions scheduled yet.
            </div>
          ) : (
            <SessionList
              sessions={sessions}
              conferenceId={id}
              onSessionClick={handleSessionClick}
            />
          )}
        </div>

        {/* Registrations section */}
        <div className="detail-section">
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '20px' }}>
            <h2 className="detail-section__title" style={{ marginBottom: 0, borderBottom: 'none' }}>
              Registrations
            </h2>
            <button
              className="btn btn--primary"
              onClick={handleRegisterClick}
              disabled={conference?.status === 'cancelled' || conference?.status === 'completed'}
            >
              Register Now
            </button>
          </div>
          {conference?.status === 'cancelled' && (
            <p style={{ color: '#6E4A2A', fontSize: '0.875rem', fontStyle: 'italic', marginBottom: '16px' }}>
              This conference has been cancelled.
            </p>
          )}
          {deleteError && (
            <div className="error-container" style={{ marginBottom: '16px', marginTop: 0 }}>
              <p>{deleteError}</p>
            </div>
          )}

          {registrationsLoading ? (
            <div className="loading-container" style={{ padding: '40px 0' }}>
              <div className="spinner" />
              <p>Loading registrations...</p>
            </div>
          ) : registrationsError ? (
            <div className="error-container">
              <h3>Failed to load registrations</h3>
              <p>{registrationsError}</p>
            </div>
          ) : registrations.length === 0 ? (
            <div style={{ color: '#6E4A2A', padding: '20px 0', textAlign: 'center', fontStyle: 'italic' }}>
              No registrations yet.
            </div>
          ) : (
            <div className="card">
              <div className="attendee-list-header">
                <span>Attendee</span>
                <span>Email</span>
                <span>Registered</span>
                <span>Status</span>
                <span></span>
              </div>
              {registrations.map(reg => {
                const a = reg.attendee || {}
                return (
                  <div key={reg.id} className="attendee-row">
                    <span className="attendee-row__name">
                      {a.firstName} {a.lastName}
                    </span>
                    <span className="attendee-row__email">{a.email || '—'}</span>
                    <span className="attendee-row__date">
                      {reg.registeredAt
                        ? new Date(reg.registeredAt).toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' })
                        : '—'}
                    </span>
                    <span>
                      <span className={`badge badge--${(reg.status || 'upcoming').toLowerCase()}`}>
                        {reg.status || 'REGISTERED'}
                      </span>
                    </span>
                    <span>
                      <button
                        className="btn btn--ghost btn--sm"
                        onClick={() => handleRemoveRegistration(reg.id)}
                        style={{ color: 'var(--error-text)', borderColor: 'var(--error-border)' }}
                      >
                        Remove
                      </button>
                    </span>
                  </div>
                )
              })}
              <div className="attendee-list-footer">
                {registrations.length} registration{registrations.length !== 1 ? 's' : ''}
              </div>
            </div>
          )}
        </div>

        {/* Selected session highlight — only shows when user has clicked a session */}
        {selectedSession && (
          <div className="detail-section">
            <h2 className="detail-section__title">Selected Session</h2>
            <div className="card" style={{ borderColor: '#6c9fff' }}>
              <div className="card__title">{selectedSession.title}</div>
              <div style={{ display: 'flex', gap: '16px', fontSize: '0.85rem', color: '#888', margin: '8px 0' }}>
                {selectedSession.startTime && <span>⏰ {selectedSession.startTime}</span>}
                {selectedSession.room && <span>🚪 {selectedSession.room.name || selectedSession.room}</span>}
                {selectedSession.capacity && <span>👥 Capacity: {selectedSession.capacity}</span>}
              </div>
              {selectedSession.description && (
                <p className="card__description" style={{ WebkitLineClamp: 'unset' }}>
                  {selectedSession.description}
                </p>
              )}
              <div style={{ marginTop: '12px', display: 'flex', gap: '8px' }}>
                <Link
                  to={`/conferences/${id}/sessions/${selectedSession.id}`}
                  className="btn btn--secondary btn--sm"
                >
                  View Full Details
                </Link>
                <button
                  className="btn btn--ghost btn--sm"
                  onClick={() => setSelectedSession(null)}
                >
                  Dismiss
                </button>
              </div>
            </div>
          </div>
        )}
      </div>

      {/* ---- Registration Modal ---- */}
      {showModal && (
        <div className="modal-overlay" onClick={e => { if (e.target === e.currentTarget) handleModalClose() }}>
          <div className="modal">
            <div className="modal__header">
              <h2 className="modal__title">
                Register for {conference?.title}
              </h2>
              <button className="modal__close" onClick={handleModalClose} aria-label="Close">
                ✕
              </button>
            </div>

            {registrationSuccess ? (
              <div className="success-box">
                <h3>Registration Successful!</h3>
                <p>
                  You have been registered for <strong>{conference?.title}</strong>.
                  A confirmation will be sent to <strong>{formData.email}</strong>.
                </p>
                <button
                  className="btn btn--secondary"
                  style={{ marginTop: '16px' }}
                  onClick={handleModalClose}
                >
                  Close
                </button>
              </div>
            ) : (
              <form onSubmit={handleRegistrationSubmit}>
                <p style={{ color: '#888', fontSize: '0.875rem', marginBottom: '20px' }}>
                  Fill in your details below to register. All fields marked * are required.
                </p>

                {registrationError && (
                  <div className="error-container" style={{ marginBottom: '16px', marginTop: 0 }}>
                    <p>{registrationError}</p>
                  </div>
                )}

                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '0 16px' }}>
                  <div className="form-group">
                    <label htmlFor="reg-firstName">First Name *</label>
                    <input
                      id="reg-firstName"
                      type="text"
                      name="firstName"
                      value={formData.firstName}
                      onChange={handleFormChange}
                      placeholder="Jane"
                      required
                    />
                  </div>

                  <div className="form-group">
                    <label htmlFor="reg-lastName">Last Name *</label>
                    <input
                      id="reg-lastName"
                      type="text"
                      name="lastName"
                      value={formData.lastName}
                      onChange={handleFormChange}
                      placeholder="Smith"
                      required
                    />
                  </div>
                </div>

                <div className="form-group">
                  <label htmlFor="reg-email">Email Address *</label>
                  <input
                    id="reg-email"
                    type="email"
                    name="email"
                    value={formData.email}
                    onChange={handleFormChange}
                    placeholder="jane.smith@example.com"
                    required
                  />
                </div>

                <div className="form-group">
                  <label htmlFor="reg-phone">Phone Number</label>
                  <input
                    id="reg-phone"
                    type="tel"
                    name="phone"
                    value={formData.phone}
                    onChange={handleFormChange}
                    placeholder="+1 (555) 000-0000"
                  />
                </div>

                <div className="form-group">
                  <label htmlFor="reg-company">Company / Organization</label>
                  <input
                    id="reg-company"
                    type="text"
                    name="company"
                    value={formData.company}
                    onChange={handleFormChange}
                    placeholder="Acme Corp"
                  />
                </div>

                <div className="form-group">
                  <label htmlFor="reg-dietary">Dietary Requirements</label>
                  <input
                    id="reg-dietary"
                    type="text"
                    name="dietaryRequirements"
                    value={formData.dietaryRequirements}
                    onChange={handleFormChange}
                    placeholder="e.g. vegetarian, gluten-free"
                  />
                </div>

                {selectedSession && (
                  <div
                    style={{
                      background: '#0f1a33',
                      borderRadius: '6px',
                      padding: '12px',
                      marginBottom: '20px',
                      fontSize: '0.875rem',
                      color: '#aaa'
                    }}
                  >
                    <strong style={{ color: '#6c9fff' }}>Session selected:</strong>{' '}
                    {selectedSession.title}
                  </div>
                )}

                <div style={{ display: 'flex', gap: '12px', justifyContent: 'flex-end' }}>
                  <button
                    type="button"
                    className="btn btn--ghost"
                    onClick={handleModalClose}
                    disabled={registrationLoading}
                  >
                    Cancel
                  </button>
                  <button
                    type="submit"
                    className="btn btn--primary"
                    disabled={registrationLoading}
                  >
                    {registrationLoading ? 'Registering...' : 'Complete Registration'}
                  </button>
                </div>
              </form>
            )}
          </div>
        </div>
      )}
    </div>
  )
}
