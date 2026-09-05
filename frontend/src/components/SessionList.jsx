import SessionCard from './SessionCard'

export default function SessionList({ sessions, conferenceId, onSessionClick }) {
  if (!sessions || sessions.length === 0) {
    return (
      <div style={{ color: '#666', padding: '20px 0', textAlign: 'center' }}>
        No sessions available.
      </div>
    )
  }

  return (
    <div>
      {sessions.map(session => (
        <SessionCard
          key={session.id}
          session={session}
          conferenceId={conferenceId}
          onSessionClick={onSessionClick}
        />
      ))}
    </div>
  )
}
