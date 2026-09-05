import { useState, useEffect } from 'react'
import { useParams, Link } from 'react-router-dom'
import { getSession } from '../api'

export default function SessionDetailPage() {
  const { id, sessionId } = useParams()

  const [session, setSession] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  useEffect(() => {
    setLoading(true)
    setError(null)
    getSession(sessionId, id)
      .then(data => {
        const s = data.data || data
        setSession(s)
        setLoading(false)
      })
      .catch(err => {
        setError(err.message || 'Failed to load session')
        setLoading(false)
      })
  }, [sessionId, id])

  function formatTime(timeString) {
    if (!timeString) return '—'
    return timeString
  }

  function formatDate(dateString) {
    if (!dateString) return '—'
    try {
      return new Date(dateString).toLocaleDateString('en-US', {
        weekday: 'long',
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
          <p>Loading session...</p>
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
            <Link to={`/conferences/${id}`}>Conference</Link>
            <span className="breadcrumb__sep">/</span>
            <span>Session</span>
          </div>
          <div className="error-container">
            <h3>Failed to load session</h3>
            <p>{error}</p>
          </div>
        </div>
      </div>
    )
  }

  const speaker = session?.speaker || session?.speakerDetails || null
  const room = session?.room || session?.roomDetails || null

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
          <Link to={`/conferences/${id}`}>
            {session?.conferenceName || 'Conference'}
          </Link>
          <span className="breadcrumb__sep">/</span>
          <span>Session</span>
        </div>

        {/* Session header */}
        <div className="detail-header">
          <h1 className="detail-header__title">{session?.title}</h1>

          <div className="detail-header__meta">
            {session?.startTime && (
              <span>⏰ {formatTime(session.startTime)}{session.endTime ? ` — ${formatTime(session.endTime)}` : ''}</span>
            )}
            {session?.date && (
              <span>📅 {formatDate(session.date)}</span>
            )}
            {session?.capacity && (
              <span>👥 Capacity: {session.capacity}</span>
            )}
            {session?.sessionType && (
              <span>🏷️ {session.sessionType}</span>
            )}
          </div>

          {session?.description && (
            <p className="detail-header__description">{session.description}</p>
          )}
        </div>

        {/* Session details */}
        <div className="detail-section">
          <h2 className="detail-section__title">Session Details</h2>
          <div className="card">
            <table className="info-table">
              <tbody>
                <tr>
                  <td>Title</td>
                  <td>{session?.title || '—'}</td>
                </tr>
                <tr>
                  <td>Type</td>
                  <td>{session?.sessionType || session?.type || '—'}</td>
                </tr>
                <tr>
                  <td>Start Time</td>
                  <td>{formatTime(session?.startTime)}</td>
                </tr>
                <tr>
                  <td>End Time</td>
                  <td>{formatTime(session?.endTime)}</td>
                </tr>
                <tr>
                  <td>Capacity</td>
                  <td>{session?.capacity ?? '—'}</td>
                </tr>
                <tr>
                  <td>Available Seats</td>
                  <td>{session?.availableSeats ?? '—'}</td>
                </tr>
                {session?.tags && session.tags.length > 0 && (
                  <tr>
                    <td>Tags</td>
                    <td>
                      <div style={{ display: 'flex', flexWrap: 'wrap', gap: '6px' }}>
                        {session.tags.map((tag, i) => (
                          <span key={i} className="badge badge--upcoming">{tag}</span>
                        ))}
                      </div>
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        </div>

        {/* Speaker info */}
        {speaker && (
          <div className="detail-section">
            <h2 className="detail-section__title">Speaker</h2>
            <div className="speaker-block">
              <div className="speaker-block__avatar">
                {speaker.firstName ? speaker.firstName[0].toUpperCase() : '?'}
                {speaker.lastName ? speaker.lastName[0].toUpperCase() : ''}
              </div>
              <div style={{ flex: 1 }}>
                <div className="speaker-block__name">
                  {speaker.firstName} {speaker.lastName}
                </div>
                {speaker.email && (
                  <div className="speaker-block__email">
                    <a href={`mailto:${speaker.email}`}>{speaker.email}</a>
                  </div>
                )}
                {speaker.title && (
                  <div style={{ fontSize: '0.85rem', color: '#aaa', marginBottom: '6px' }}>
                    {speaker.title}{speaker.company ? ` at ${speaker.company}` : ''}
                  </div>
                )}
                {speaker.bio && (
                  <p className="speaker-block__bio">{speaker.bio}</p>
                )}
              </div>
            </div>
          </div>
        )}

        {/* Room info */}
        {room && (
          <div className="detail-section">
            <h2 className="detail-section__title">Room</h2>
            <div className="card">
              <table className="info-table">
                <tbody>
                  <tr>
                    <td>Room Name</td>
                    <td>{room.name || room.roomName || '—'}</td>
                  </tr>
                  {room.location && (
                    <tr>
                      <td>Location</td>
                      <td>{room.location}</td>
                    </tr>
                  )}
                  {room.capacity && (
                    <tr>
                      <td>Capacity</td>
                      <td>{room.capacity}</td>
                    </tr>
                  )}
                  {room.floor && (
                    <tr>
                      <td>Floor</td>
                      <td>{room.floor}</td>
                    </tr>
                  )}
                  {room.building && (
                    <tr>
                      <td>Building</td>
                      <td>{room.building}</td>
                    </tr>
                  )}
                </tbody>
              </table>
            </div>
          </div>
        )}

        {/* Back link */}
        <div style={{ marginTop: '24px' }}>
          <Link to={`/conferences/${id}`} className="btn btn--ghost">
            ← Back to Conference
          </Link>
        </div>
      </div>
    </div>
  )
}
