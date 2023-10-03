<#macro navbar activeBarItem>
    <style>
        <#include "css/style.css"/>
    </style>

        <header class="window-header">
            <nav class="navigation">
                <div class="navigation-top">
                    <a href="/deliverylayer/ui/home"
                       class="navigation-item <#if activeBarItem == 1>active</#if>">
                        <svg width="24" height="24" viewBox="0 0 24 24" fill="none"
                             xmlns="http://www.w3.org/2000/svg">
                            <path d="M3 9.5L12 4L21 9.5" stroke="currentColor" stroke-width="1.5"
                                  stroke-linecap="round" stroke-linejoin="round"/>
                            <path d="M19 13V19.4C19 19.7314 18.7314 20 18.4 20H5.6C5.26863 20 5 19.7314 5 19.4V13"
                                  stroke="currentColor" stroke-width="1.5" stroke-linecap="round"
                                  stroke-linejoin="round"/>
                        </svg>
                        <span class="navigation-item-title">Startseite</span>
                    </a>
                    <a href="#" class="navigation-item <#if activeBarItem == 2>active</#if>">
                        <svg width="24" height="24" viewBox="0 0 24 24" fill="none"
                             xmlns="http://www.w3.org/2000/svg">
                            <path d="M3.6 3H20.4C20.7314 3 21 3.26863 21 3.6V20.4C21 20.7314 20.7314 21 20.4 21H3.6C3.26863 21 3 20.7314 3 20.4V3.6C3 3.26863 3.26863 3 3.6 3Z"
                                  stroke="currentColor" stroke-width="1.5"/>
                            <path d="M9.75 9.75V21" stroke="currentColor" stroke-width="1.5"/>
                            <path d="M3 9.75H21" stroke="currentColor" stroke-width="1.5"/>
                        </svg>
                        <span class="navigation-item-title">Pages</span>
                    </a>
                    <a href="/deliverylayer/ui/clients"
                       class="navigation-item <#if activeBarItem == 3>active</#if>">
                        <svg width="24" height="24" viewBox="0 0 24 24" fill="none"
                             xmlns="http://www.w3.org/2000/svg">
                            <path d="M2.6954 7.18536L11.6954 11.1854L12.3046 9.81464L3.3046 5.81464L2.6954 7.18536ZM12.75 21.5V10.5H11.25V21.5H12.75ZM12.3046 11.1854L21.3046 7.18536L20.6954 5.81464L11.6954 9.81464L12.3046 11.1854Z"
                                  fill="currentColor"/>
                            <path d="M3 17.1101V6.88992C3 6.65281 3.13964 6.43794 3.35632 6.34164L11.7563 2.6083C11.9115 2.53935 12.0885 2.53935 12.2437 2.6083L20.6437 6.34164C20.8604 6.43794 21 6.65281 21 6.88992V17.1101C21 17.3472 20.8604 17.5621 20.6437 17.6584L12.2437 21.3917C12.0885 21.4606 11.9115 21.4606 11.7563 21.3917L3.35632 17.6584C3.13964 17.5621 3 17.3472 3 17.1101Z"
                                  stroke="currentColor" stroke-width="1.5" stroke-linecap="round"
                                  stroke-linejoin="round"/>
                            <path d="M7.5 4.5L16.1437 8.34164C16.3604 8.43794 16.5 8.65281 16.5 8.88992V12.5"
                                  stroke="currentColor" stroke-width="1.5" stroke-linecap="round"
                                  stroke-linejoin="round"/>
                        </svg>
                        <span class="navigation-item-title">Clients</span>
                    </a>
                    <a href="/swagger-ui/index.html" class="navigation-item">
                        <svg width="24" height="24" xmlns="http://www.w3.org/2000/svg"
                             viewBox="0 0 100 100" id="code">
                            <path style="fill: white;stroke:white;stroke-width:4;opacity: 100%"
                                  d="M30.4 29.4 9.8 50l20.6 20.6c.8.8.8 2 0 2.8-.4.4-.9.6-1.4.6s-1-.2-1.4-.6l-22-22c-.8-.8-.8-2 0-2.8l22-22c.8-.8 2-.8 2.8 0 .8.8.8 2 0 2.8zm64 19.2-22-22c-.8-.8-2-.8-2.8 0-.8.8-.8 2 0 2.8L90.2 50 69.6 70.6c-.8.8-.8 2 0 2.8.4.4.9.6 1.4.6s1-.2 1.4-.6l22-22c.8-.8.8-2 0-2.8zM61.6 14.1c-1-.3-2.2.2-2.5 1.3l-22 68c-.3 1.1.2 2.2 1.3 2.5.2.1.4.1.6.1.8 0 1.6-.5 1.9-1.4l22-68c.3-1-.2-2.2-1.3-2.5z"></path>
                        </svg>
                        <span class="navigation-item-title">Swagger UI</span>
                    </a>
                </div>
                <div class="navigation-bottom">
                    <a href="#" class="navigation-item">
                        <img class="profile-picture-image" src="${profilePicture}" alt="Hallo"/>
                        <span class="navigation-item-title">Your Profile</span>
                    </a>
                </div>
            </nav>
        </header>
</#macro>
