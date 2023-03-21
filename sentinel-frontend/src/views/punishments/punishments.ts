export const getPunishments = async () => {
  const response = await fetch(`${import.meta.env.VITE_SENTINEL_API_URL}/v1/punishments`)
  if (!response.ok) throw new Error(`Error status code: ${response.status}`)
  return await response.json()
}

export const getPunishment = async (id: string) => {
  const response = await fetch(`${import.meta.env.VITE_SENTINEL_API_URL}/v1/punishment/${id}`)
  if (!response.ok) throw new Error(`Error status code: ${response.status}`)
  return await response.json()
}
