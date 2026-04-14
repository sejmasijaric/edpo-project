import { Moon, ShoppingCart, Sun, Wrench } from "lucide-react"
import { Button } from "@/components/ui/button"
import { Separator } from "@/components/ui/separator"
import { useTheme } from "@/components/theme-provider"

export type Page = "customer" | "worker"

interface AppSidebarProps {
  activePage: Page
  onNavigate: (page: Page) => void
}

export function AppSidebar({ activePage, onNavigate }: AppSidebarProps) {
  const { theme, setTheme } = useTheme()

  const toggleTheme = () => {
    setTheme(theme === "dark" ? "light" : "dark")
  }

  return (
    <aside className="flex h-screen w-64 flex-col border-r bg-sidebar text-sidebar-foreground">
      <div className="p-4">
        <h2 className="text-lg font-semibold">Air Tag Orders</h2>
      </div>
      <Separator />
      <nav className="flex flex-1 flex-col gap-1 p-2">
        <Button
          variant={activePage === "customer" ? "secondary" : "ghost"}
          className="justify-start gap-2"
          onClick={() => onNavigate("customer")}
        >
          <ShoppingCart className="size-4" />
          Customer
        </Button>
        <Button
          variant={activePage === "worker" ? "secondary" : "ghost"}
          className="justify-start gap-2"
          onClick={() => onNavigate("worker")}
        >
          <Wrench className="size-4" />
          Worker
        </Button>
      </nav>
      <Separator />
      <div className="p-2">
        <Button
          variant="ghost"
          className="w-full justify-start gap-2"
          onClick={toggleTheme}
          aria-label="Toggle theme"
        >
          {theme === "dark" ? (
            <Sun className="size-4" />
          ) : (
            <Moon className="size-4" />
          )}
          {theme === "dark" ? "Light mode" : "Dark mode"}
        </Button>
      </div>
    </aside>
  )
}
