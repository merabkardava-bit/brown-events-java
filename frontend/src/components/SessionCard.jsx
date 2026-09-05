import SessionMeta from './SessionMeta'

export default function SessionCard({ session, conferenceId, onSessionClick }) {
  return (
    <div className="session-card" onClick={() => onSessionClick(session)}>
      <div className="session-card__title">{session.title}</div>

      <SessionMeta session={session} conferenceId={conferenceId} />

      {session.description && (
        <p className="session-card__description">{session.description}</p>
      )}
    </div>
  )
}
