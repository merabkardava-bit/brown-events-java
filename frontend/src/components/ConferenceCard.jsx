import { Link } from 'react-router-dom'

export default function ConferenceCard({ conference }) {
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
    if (!dateString) return null
    try {
      return new Date(dateString).toLocaleDateString('en-US', {
        month: 'short',
        day: 'numeric',
        year: 'numeric'
      })
    } catch {
      return dateString
    }
  }

  return (
    <Link to={`/conferences/${conference.id}`} style={{ textDecoration: 'none' }}>
      <div className="card">
        <div style={{ display: 'flex', alignItems: 'flex-start', justifyContent: 'space-between', marginBottom: '8px' }}>
          <div className="card__title" style={{ marginBottom: 0, flex: 1, paddingRight: '8px' }}>
            {conference.title}
          </div>
          <span className={getStatusBadgeClass(conference.status)}>
            {conference.status || 'Upcoming'}
          </span>
        </div>

        {conference.description && (
          <p className="card__description">{conference.description}</p>
        )}

        <div className="card__meta">
          {conference.location && (
            <span className="card__meta-item">
              📍 {conference.location}
            </span>
          )}
          {conference.startDate && (
            <span className="card__meta-item">
              📅 {formatDate(conference.startDate)}
              {conference.endDate && ` — ${formatDate(conference.endDate)}`}
            </span>
          )}
          {conference.maxAttendees && (
            <span className="card__meta-item">
              👥 {conference.maxAttendees} seats
            </span>
          )}
        </div>
      </div>
    </Link>
  )
}
