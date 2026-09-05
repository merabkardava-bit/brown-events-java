import { Link } from 'react-router-dom'

export default function SessionMeta({ session, conferenceId }) {
  function formatTime(timeString) {
    if (!timeString) return null
    return timeString
  }

  return (
    <div className="session-card__meta">
      {session.startTime && (
        <span>
          ⏰ {formatTime(session.startTime)}
          {session.endTime ? ` — ${formatTime(session.endTime)}` : ''}
        </span>
      )}
      {session.room && (
        <span>🚪 {session.room.name || session.room}</span>
      )}
      {session.capacity && (
        <span>👥 {session.capacity} seats</span>
      )}
      {session.sessionType && (
        <span>🏷️ {session.sessionType}</span>
      )}
      {session.speakerName && (
        <span>🎤 {session.speakerName}</span>
      )}
      <Link
        to={`/conferences/${conferenceId}/sessions/${session.id}`}
        className="btn btn--secondary btn--sm"
        onClick={e => e.stopPropagation()}
      >
        View Details
      </Link>
    </div>
  )
}
