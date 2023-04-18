export const getFactoids = async () => {
  const response = await fetch(`${import.meta.env.VITE_SENTINEL_API_URL}/v1/factoids/${import.meta.env.VITE_SENTINEL_GUILD}`)
  if (!response.ok) throw new Error(`Error status code: ${response.status}`)
  return await response.json()
}

export const getFactoid = async (id: string) => {
  const response = await fetch(`${import.meta.env.VITE_SENTINEL_API_URL}/v1/factoids/${import.meta.env.VITE_SENTINEL_GUILD}/${id}`)
  if (!response.ok) throw new Error(`Error status code: ${response.status}`)
  return await response.json()
}
